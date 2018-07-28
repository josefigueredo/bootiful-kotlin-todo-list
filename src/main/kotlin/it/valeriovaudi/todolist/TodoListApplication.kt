package it.valeriovaudi.todolist

import it.valeriovaudi.todolist.web.config.RepositoryConfig
import it.valeriovaudi.todolist.web.config.RouteConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class TodoListApplication

fun main(args: Array<String>) {

    runApplication<TodoListApplication>(*args) {
        addInitializers(RepositoryConfig.beans())
        addInitializers(RouteConfig.routes())
    }
}