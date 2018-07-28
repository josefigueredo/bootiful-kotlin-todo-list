package it.valeriovaudi.todolist.adapter.repository

import it.valeriovaudi.todolist.core.model.Todo
import it.valeriovaudi.todolist.core.repository.TodoRepository
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TodoMongoRepository(private val reactiveMongoTemplate: ReactiveMongoTemplate) : TodoRepository {

    override fun insert(todo: Todo): Mono<Todo> = reactiveMongoTemplate.save(todo)

    override fun findOne(todoId: String, userName: String): Mono<Todo> =
            reactiveMongoTemplate.findOne(Query.query(Criteria("id").`is`(todoId)
                    .and("userName").`is`(userName)),
                    Todo::class.java)

    override fun findAll(userName: String, date: LocalDate): Flux<Todo> = reactiveMongoTemplate.find(Query.query(Criteria("userName").`is`(userName).and("date")
            .gte(LocalDateTime.of(date, LocalTime.MIN)).lte(LocalDateTime.of(date, LocalTime.MAX))), Todo::class.java);

    override fun update(userName: String, todoId: String, todo: String) = findOne(todoId, userName).flatMap { Mono.just(it.copy(id = it.id,userName = it.userName,date = it.date, todo = todo)) }.flatMap { reactiveMongoTemplate.save(it) }

    override fun delete(todoId: String, userName: String) = findOne(todoId, userName).flatMap { reactiveMongoTemplate.remove(it).then(Mono.just(it)) }
}