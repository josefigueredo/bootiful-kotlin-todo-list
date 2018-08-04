package it.valeriovaudi.todolist.web.representation

import java.time.LocalDateTime

data class TodoRepresentation(var id: String? = null, val date: LocalDateTime = LocalDateTime.now(), val todo: String = "")
