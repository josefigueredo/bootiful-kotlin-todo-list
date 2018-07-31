package it.valeriovaudi.todolist.web.config

import it.valeriovaudi.todolist.core.model.Todo
import it.valeriovaudi.todolist.core.repository.TodoRepository
import it.valeriovaudi.todolist.web.representation.TodoRepresentation
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object RouteConfig {


    fun routes() = beans {
        bean {
            val todoRepository = ref<TodoRepository>()

            router {
                POST("/todo/item") {
                    Mono.zip(it.principal(), it.bodyToMono(TodoRepresentation::class.java))
                            .flatMap { todoRepository.insert(Todo(it.t2.id, it.t1.name, it.t2.date, it.t2.todo)) }
                            .flatMap { ServerResponse.created(URI.create("/todo/item/%s".format(it.id))).build() }
                }

                GET("/todo/item/{id}") {
                    val todoId = it.pathVariable("id")
                    it.principal()
                            .flatMap { todoRepository.findOne(todoId, it.name) }
                            .map { Mono.just(TodoRepresentation(it.id, it.date, it.todo)) }
                            .flatMap { ServerResponse.ok().body(it, TodoRepresentation::class.java) }

                }

                GET("/todo/item") {
                    val day = it.queryParam("day")
                            .map { it.toLong() }
                            .map {
                                val ofEpochMilli = Instant.ofEpochMilli(it)
                                ZonedDateTime.ofInstant(ofEpochMilli, ZoneId.of("UTC"))
                                        .toLocalDate()
                            }.orElse(LocalDate.now())

                    it.principal()
                            .flatMap { ServerResponse.ok().body(todoRepository.findAll(it.name, day).map { TodoRepresentation(it.id, it.date, it.todo) }, TodoRepresentation::class.java) }
                }

                PUT("/todo/item/{id}") {
                    val todoId = it.pathVariable("id")
                    Mono.zip(it.principal(), it.bodyToMono(TodoRepresentation::class.java))
                            .flatMap { todoRepository.update(it.t1.name, todoId, it.t2.todo) }
                            .flatMap { ServerResponse.noContent().build() }
                }

                DELETE("/todo/item/{id}") {
                    val todoId = it.pathVariable("id")
                    it.principal()
                            .flatMap { todoRepository.delete(todoId, it.name) }
                            .flatMap { ServerResponse.noContent().build() }
                }
            }
        }
    }

}