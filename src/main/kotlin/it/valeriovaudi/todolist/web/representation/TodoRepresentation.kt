package it.valeriovaudi.todolist.web.representation

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class TodoRepresentation(@Id var id: String? = null, val date: LocalDateTime = LocalDateTime.now(), val todo: String = "")
