package it.valeriovaudi.todolist.web.config

import it.valeriovaudi.todolist.adapter.repository.TodoMongoRepository

object RepositoryConfig {

    fun beans() = org.springframework.context.support.beans {
        bean<TodoMongoRepository>()
    }

}