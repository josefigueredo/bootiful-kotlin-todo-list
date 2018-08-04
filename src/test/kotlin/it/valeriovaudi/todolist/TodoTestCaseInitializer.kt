package it.valeriovaudi.todolist

import it.valeriovaudi.todolist.web.representation.TodoRepresentation
import it.valeriovaudi.todolist.core.model.Todo
import it.valeriovaudi.todolist.core.repository.TodoRepository
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.time.LocalDateTime
import java.util.*

object TodoTestCaseInitializer {

    val now = LocalDateTime.now()

    val todoId_1 = UUID.randomUUID().toString();
    val todoId_2 = UUID.randomUUID().toString();
    val todoId_3 = UUID.randomUUID().toString();

    fun todo1() = Todo(todoId_1, "user", now, "todo1")
    fun todo2() = Todo(todoId_2, "user", now, "todo2")
    fun todo3() = Todo(todoId_3, "user", now, "todo3")

    fun initMongo(todoMongoRepository: TodoRepository) =
            Mono.zip(todoMongoRepository.insert(todo1()).toMono(),
                    todoMongoRepository.insert(todo2()).toMono(),
                    todoMongoRepository.insert(todo3()).toMono())


    fun tearDownMongo(todoMongoRepository: TodoRepository) =
            Mono.zip(todoMongoRepository.delete(todoId = todo1().id!!, userName = "user").toMono(),
                    todoMongoRepository.delete(todoId = todo2().id!!, userName = "user").toMono(),
                    todoMongoRepository.delete(todoId = todo3().id!!, userName = "user").toMono())


    fun giveAnOrderedTodoListById() = listOf(todo1(), todo2(), todo3()).sortedBy { it.id }

    fun giveAnOrderedTodoListByIdFor(todoList: List<Todo>) = todoList.sortedBy { it.id }

    fun giveAnOrderedTodoRepresentationListById() = listOf(todo1(), todo2(), todo3())
            .map { TodoRepresentation(it.id, it.date, it.todo) }.sortedBy { it.id }

    fun giveAnOrderedTodoRepresentationListByIdFor(todoRepresentationList: List<TodoRepresentation>) = todoRepresentationList.sortedBy { it.id }
}