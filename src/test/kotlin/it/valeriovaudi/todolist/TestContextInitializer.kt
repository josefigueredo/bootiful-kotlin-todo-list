package it.valeriovaudi.todolist

import it.valeriovaudi.todolist.web.config.RepositoryConfig
import it.valeriovaudi.todolist.web.config.RouteConfig
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext

class TestContextInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(applicationContext: GenericApplicationContext) {
        RepositoryConfig.beans().initialize(applicationContext)
        RouteConfig.routes().initialize(applicationContext)
    }
}