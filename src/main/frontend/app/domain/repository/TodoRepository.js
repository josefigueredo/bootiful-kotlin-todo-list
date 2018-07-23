export default class TodoRepository {

    insert(todo) {
    }

    readAll(today) {
        return fetch(`/todo/item?day=${today}`, {
            method: "GET",
            headers: {
                "Accept": "application/json"
            },
            credentials: 'same-origin'
        }).then((response => response.json()))
    }

    read(todoId) {
    }
}