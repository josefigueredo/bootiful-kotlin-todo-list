export default class TodoRepository {

    delete(todoId) {
        return fetch(`/todo/item/${todoId}`, {
            method: "DELETE",
            credentials: 'same-origin'
        }).then((response => new Promise((resolve, reject) => {
            resolve(response.status === 204)
        })));
    }

    insert(todo) {
        return fetch('/todo/item', {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            },
            body: JSON.stringify(todo),
            credentials: 'same-origin'
        }).then((response => response.json()))
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
}