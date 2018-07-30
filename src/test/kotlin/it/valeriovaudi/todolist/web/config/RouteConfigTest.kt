package it.valeriovaudi.todolist.web.config

import it.valeriovaudi.todolist.TestContextInitializer
import it.valeriovaudi.todolist.TodoTestCaseInitializer
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
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*


@ContextConfiguration(initializers = [TestContextInitializer::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner::class)
class RouteConfigTest {

    val now = TodoTestCaseInitializer.now

    val todo1 = TodoTestCaseInitializer.todo1()
    val todo2 = TodoTestCaseInitializer.todo2()
    val todo3 = TodoTestCaseInitializer.todo3()


    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var todoMongoRepository: TodoRepository

    @Before
    fun setUp() {
        TodoTestCaseInitializer.initMongo(todoMongoRepository).blockOptional()
    }

    @After
    fun tearDown() {
        TodoTestCaseInitializer.tearDownMongo(todoMongoRepository).blockOptional()
    }

    @Test
    @WithMockUser
    fun `find all the todo in the list`() {
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

    @Test
    @WithMockUser(username = "user")
    fun `insert an other todo in the list`() {
        val todo = TodoRepresentation(todo = "a todo", date = now)

        val exchange = this.webClient.post().uri("/todo/item")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(todo))
                .exchange();

        val newTodoId = exchange
                .returnResult<Unit>()
                .responseHeaders["Location"]
                .orEmpty().first()
                .split("/").last()


        val actual = todoMongoRepository.findOne(newTodoId, "user").block();

        Assert.assertThat(actual, Is.`is`(Todo(newTodoId, "user", now, "a todo")))
    }


    fun `read a specific todo`() {

    }
}