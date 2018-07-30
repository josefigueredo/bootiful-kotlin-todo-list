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
import org.springframework.web.reactive.function.BodyInserter
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
        val actual = TodoTestCaseInitializer.giveAnOrderedTodoRepresentationListByIdFor(
                this.webClient.get().uri("/todo/item").exchange().expectStatus().isOk
                        .returnResult(TodoRepresentation::class.java)
                        .responseBody.collectList().block()
                        .orEmpty())


        val expected = TodoTestCaseInitializer.giveAnOrderedTodoRepresentationListById()

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


    @Test
    @WithMockUser("user")
    fun `read a specific todo`() {
        val actual = this.webClient.get().uri("/todo/item/${todo1.id}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(TodoRepresentation::class.java)
                .returnResult()
                .responseBody

        Assert.assertThat(actual, Is.`is`(TodoRepresentation(todo1.id, todo1.date, todo1.todo)))

    }

    @Test
    @WithMockUser("user")
    fun `update a specific todo`() {
        this.webClient.put().uri("/todo/item/${todo1.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(TodoRepresentation(todo="a new Todo in list")))
                .exchange()
        val actual = todoMongoRepository.findOne(todo1.id!!, "user")
                .map{ TodoRepresentation(it.id, it.date, it.todo) }
                .block()

        Assert.assertThat(actual, Is.`is`(TodoRepresentation(todo1.id, todo1.date, "a new Todo in list")))

    }
}