package it.valeriovaudi.todolist.core.model

import java.time.LocalDateTime

data class Todo(var id: String? = null, val userName: String?, val date: LocalDateTime = LocalDateTime.now(), val todo: String = "")
