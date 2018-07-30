package it.valeriovaudi.todolist.adapter.repository

import it.valeriovaudi.todolist.core.model.Todo
import it.valeriovaudi.todolist.core.repository.TodoRepository
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


@DataMongoTest
@RunWith(SpringRunner::class)
class TodoMongoRepositoryTest {

    var todoMongoRepository: TodoRepository? = null;
    val todo1 = Todo(UUID.randomUUID().toString(), "user", LocalDateTime.now(), "todo1")
    val todo2 = Todo(UUID.randomUUID().toString(), "user", LocalDateTime.now(), "todo2")
    val todo3 = Todo(UUID.randomUUID().toString(), "user", LocalDateTime.now(), "todo3")

    @Autowired
    private val reactiveMongoRepository: ReactiveMongoTemplate? = null

    @Before
    fun setUp() {
        todoMongoRepository = TodoMongoRepository(reactiveMongoRepository!!);

        Mono.zip(todoMongoRepository!!.insert(todo1),
                todoMongoRepository!!.insert(todo2),
                todoMongoRepository!!.insert(todo3))
                .blockOptional()
    }

    @After
    fun tearDown() {
        Mono.zip(todoMongoRepository!!.delete(todoId = todo1.id!!, userName = "user"),
                todoMongoRepository!!.delete(todoId = todo2.id!!, userName = "user"),
                todoMongoRepository!!.delete(todoId = todo3.id!!, userName = "user"))
                .blockOptional()
    }

    @Test
    fun `insert a new Todo in Mongo db`() {

        val todo = Todo(UUID.randomUUID().toString(), "user", LocalDateTime.now(), "todo")
        val actual = todoMongoRepository!!.insert(todo)
                .block(Duration.ofMinutes(1))
        Assert.assertThat(actual, Is.`is`(todo))
    }

    @Test
    fun `read a set of daily todo per user in Mongo db`() {
        val mutableListOf = mutableListOf(todo1, todo2, todo3);

        todoMongoRepository!!.findAll("user", LocalDate.now())
                .collectList()
                .block(Duration.ofMinutes(1))
                .orEmpty()
                .forEach { mutableListOf.remove(it) }
        Assert.assertThat(mutableListOf.size, Is.`is`(0))
    }

    @Test
    fun `read a specific todo per user in Mongo db`() {
        val todo = todoMongoRepository!!.findOne(todoId = todo1.id!!, userName = "user")
                .block(Duration.ofMinutes(1))

        Assert.assertThat(todo, Is.`is`(todo1))
    }

    @Test
    fun `delete a specific todo per user in Mongo db`() {
        val mutableListOf = mutableListOf(todo2, todo3)
        todoMongoRepository!!.delete(todoId = todo1.id!!, userName = "user")
                .block(Duration.ofMinutes(1))

        todoMongoRepository!!.findAll("user", LocalDate.now())
                .collectList()
                .block(Duration.ofMinutes(1))
                .orEmpty()
                .forEach { mutableListOf.remove(it) }
        Assert.assertThat(mutableListOf.size, Is.`is`(0))
    }


}