package it.valeriovaudi.todolist.adapter.repository

import it.valeriovaudi.todolist.TodoTestCaseInitializer
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.junit4.SpringRunner
import java.time.Duration
import java.time.LocalDate


@DataMongoTest
@RunWith(SpringRunner::class)
class TodoMongoRepositoryTest {

    val todo1 = TodoTestCaseInitializer.todo1()
    val todo2 = TodoTestCaseInitializer.todo2()
    val todo3 = TodoTestCaseInitializer.todo3()

    lateinit var todoMongoRepository: TodoMongoRepository;

    @Autowired
    private lateinit var reactiveMongoRepository: ReactiveMongoTemplate

    @Before
    fun setUp() {
        todoMongoRepository = TodoMongoRepository(reactiveMongoRepository);
        TodoTestCaseInitializer.initMongo(todoMongoRepository).blockOptional()
    }

    @After
    fun tearDown() {
        TodoTestCaseInitializer.tearDownMongo(todoMongoRepository).blockOptional()
    }

    @Test
    fun `insert a new Todo in Mongo db`() {

        val todo = todo1
        val actual = todoMongoRepository.insert(todo)
                .block(Duration.ofMinutes(1))
        Assert.assertThat(actual, Is.`is`(todo))
    }

    @Test
    fun `read a set of daily todo per user in Mongo db`() {
        val expected =
                TodoTestCaseInitializer.giveAnOrderedTodoListByIdFor(todoMongoRepository.findAll("user", LocalDate.now())
                        .collectList()
                        .block(Duration.ofMinutes(1))
                        .orEmpty())

        Assert.assertThat(TodoTestCaseInitializer.giveAnOrderedTodoListById(), Is.`is`(expected))
    }

    @Test
    fun `read a specific todo per user in Mongo db`() {
        val todo = todoMongoRepository.findOne(todoId = todo1.id!!, userName = "user")
                .block(Duration.ofMinutes(1))

        Assert.assertThat(todo, Is.`is`(todo1))
    }

    @Test
    fun `delete a specific todo per user in Mongo db`() {
        todoMongoRepository.delete(todoId = todo1.id!!, userName = "user")
                .block(Duration.ofMinutes(1))

        val actual =
                TodoTestCaseInitializer.giveAnOrderedTodoListByIdFor(todoMongoRepository.findAll("user", LocalDate.now())
                        .collectList()
                        .block(Duration.ofMinutes(1))
                        .orEmpty())

        Assert.assertThat(actual, Is.`is`(TodoTestCaseInitializer.giveAnOrderedTodoListByIdFor(listOf(todo2, todo3))))
    }


}