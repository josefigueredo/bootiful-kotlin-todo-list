package it.valeriovaudi.todolist.core.repository

import it.valeriovaudi.todolist.core.model.Todo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface TodoRepository {

    fun insert(todo: Todo): Mono<Todo>
    fun findOne(todoId: String, userName: String): Mono<Todo>
    fun findAll(userName: String, date: LocalDate): Flux<Todo>
    fun update(userName: String, todoId: String, todo: String) : Mono<Todo>
    fun delete(todoId: String, userName: String) : Mono<Todo>
}