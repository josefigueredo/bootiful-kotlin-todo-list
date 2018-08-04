package it.valeriovaudi.todolist.core.repository

import it.valeriovaudi.todolist.core.model.Todo
import org.reactivestreams.Publisher
import java.time.LocalDate

interface TodoRepository {

    fun insert(todo: Todo): Publisher<Todo>
    fun findOne(todoId: String, userName: String): Publisher<Todo>
    fun findAll(userName: String, date: LocalDate): Publisher<Todo>
    fun update(userName: String, todoId: String, todo: String) : Publisher<Todo>
    fun delete(todoId: String, userName: String) : Publisher<Todo>
}