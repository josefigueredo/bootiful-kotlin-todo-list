package it.valeriovaudi.todolist.web.config

import it.valeriovaudi.todolist.TestContextInitializer
import it.valeriovaudi.todolist.adapter.repository.TodoMongoRepository
import it.valeriovaudi.todolist.core.model.Todo
import it.valeriovaudi.todolist.core.repository.TodoRepository
import it.valeriovaudi.todolist.web.representation.TodoRepresentation
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*


@ContextConfiguration(initializers = [TestContextInitializer::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner::class)
class RouteConfigTest {

    val now = LocalDateTime.now()

    val todo1 = Todo(UUID.randomUUID().toString(), "user", now, "todo1")
    val todo2 = Todo(UUID.randomUUID().toString(), "user", now, "todo2")
    val todo3 = Todo(UUID.randomUUID().toString(), "user", now, "todo3")


    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var todoMongoRepository: TodoRepository

    @Before
    fun setUp() {
        Mono.zip(todoMongoRepository.insert(todo1),
                todoMongoRepository.insert(todo2),
                todoMongoRepository.insert(todo3))
                .blockOptional()
    }

    @After
    fun tearDown() {
        Mono.zip(todoMongoRepository.delete(todoId = todo1.id!!, userName = "user"),
                todoMongoRepository.delete(todoId = todo2.id!!, userName = "user"),
                todoMongoRepository.delete(todoId = todo3.id!!, userName = "user"))
                .blockOptional()
    }

    @Test
    @WithMockUser
    fun `first all the todo in the list`() {
        val actual = this.webClient.get().uri("/todo/item").exchange().expectStatus().isOk
                .returnResult(Todo::class.java)
                .responseBody.collectList().block()
                .orEmpty()
                .map { TodoRepresentation(it.id, it.date, it.todo) }
                .sortedBy { it.id }


        val expected = listOf(TodoRepresentation(todo1.id, now, todo1.todo),
                TodoRepresentation(todo2.id, now, todo2.todo),
                TodoRepresentation(todo3.id, now, todo3.todo))
                .sortedBy { it.id }

        Assert.assertThat(actual, Is.`is`(expected))
    }


}

