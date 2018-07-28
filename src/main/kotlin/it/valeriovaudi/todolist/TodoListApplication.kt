package it.valeriovaudi.todolist

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.beans
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.time.*


@SpringBootApplication
class TodoListApplication

fun main(args: Array<String>) {

    runApplication<TodoListApplication>(*args) {
        addInitializers(Config.beans())
        addInitializers(Config.routes())
    }
}

@Configuration
@EnableWebFluxSecurity
class SecurityCOnfig {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf().disable()
                .authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .httpBasic().and()
                .formLogin();
        return http.build();
    }

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): ReactiveUserDetailsService {
        return MapReactiveUserDetailsService(User("user", passwordEncoder.encode("secret"), listOf(SimpleGrantedAuthority("CREATOR"), SimpleGrantedAuthority("CHECKER"))));
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
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
                            }
                            .orElse(LocalDate.now());
                    it.principal()
                            .flatMap { ServerResponse.ok().body(todoRepository.findAll(it.name, day), Todo::class.java) }
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

@Document
data class Todo(@Id var id: String? = null, val userName: String?, val date: LocalDateTime = LocalDateTime.now(), val todo: String = "")

data class TodoRepresentation(@Id var id: String? = null, val date: LocalDateTime = LocalDateTime.now(), val todo: String = "")

class TodoRepository(private val reactiveMongoTemplate: ReactiveMongoTemplate) {

    fun insert(todo: Todo): Mono<Todo> = reactiveMongoTemplate.save(todo)

    fun findOne(todoId: String, userName: String): Mono<Todo> =
            reactiveMongoTemplate.findOne(Query.query(Criteria("id").`is`(todoId)
                    .and("userName").`is`(userName)),
                    Todo::class.java)

    fun findAll(userName: String, date: LocalDate): Flux<Todo> = reactiveMongoTemplate.find(Query.query(Criteria("userName").`is`(userName).and("date")
            .gte(LocalDateTime.of(date, LocalTime.MIN)).lte(LocalDateTime.of(date, LocalTime.MAX))), Todo::class.java);

    fun update(userName: String, todoId: String, todo: String) = findOne(todoId, userName).flatMap { Mono.just(it.copy(id = it.id,userName = it.userName,date = it.date, todo = todo)) }.flatMap { reactiveMongoTemplate.save(it) }

    fun delete(todoId: String, userName: String) = findOne(todoId, userName).flatMap { reactiveMongoTemplate.remove(it) }
}