package it.valeriovaudi.todolist

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDateTime


@SpringBootApplication
class TodoListApplication

fun main(args: Array<String>) {

    runApplication<TodoListApplication>(*args) {
        addInitializers(Config.beans())
        addInitializers(Config.routes())
    }
}

object Config {

    fun beans() = beans {
        bean<TodoRepository>()
    }

    fun routes() = beans {
        bean {
            router {
                val todoRepository = ref<TodoRepository>()
                POST("/todo/item") {
                    it.bodyToMono(Todo().javaClass)
                            .flatMap { todoRepository.insert(it) }
                            .flatMap { ServerResponse.created(URI.create("/todo/item/%s".format(it.id))).build() }
                }

                GET("/todo/item/{id}") {
                    ServerResponse.ok().body(todoRepository.findOne(it.pathVariable("id")), Todo::class.java)
                }

                GET("/todo/item") {
                    ServerResponse.ok().body(todoRepository.findAll(), Todo::class.java)
                }
            }
        }
    }


}

@Document
data class Todo(@Id var id: String? = null, val data: LocalDateTime = LocalDateTime.now(), val todo: String = "")

class TodoRepository(private val reactiveMongoTemplate: ReactiveMongoTemplate) {

    fun insert(todo: Todo): Mono<Todo> = reactiveMongoTemplate.save(todo)

    fun findOne(todoId: String): Mono<Todo> = reactiveMongoTemplate.findById(todoId, Todo::class.java)

    fun findAll(): Flux<Todo> = reactiveMongoTemplate.findAll(Todo().javaClass);
}