package it.valeriovaudi.todolist.core.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class Todo(@Id var id: String? = null, val userName: String?, val date: LocalDateTime = LocalDateTime.now(), val todo: String = "")
