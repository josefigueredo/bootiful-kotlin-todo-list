export default class NewTodoUseCase {

    constructor(todoRepository, updateStateCallback) {
        this.updateStateCallback = updateStateCallback;
        this.todoRepository = todoRepository;
    }


    updateTodo(todoId, todoTextValue) {
        this.todoRepository.update(todoId, todoTextValue)
            .then(result => {
                if (result === true) { this.updateStateCallback(todoId, todoTextValue)}
            })
    };

}