import React from 'react';
import TodoRepository from "./domain/repository/TodoRepository";
import TodoItemList from "./component/TodoItemList";
import NewTodoItem from "./component/NewTodoItem";
import HeaderRow from "./component/HeaderRow";
import UpdateTodoItemPopUp from "./component/UpdateTodoItemPopUp";

class App extends React.Component {

    constructor(props) {
        super(props);
        this.todoRepository = new TodoRepository();
        this.state = {todoItems: []};
        this.newTodoInputRef = React.createRef();
        this.updateTodoInputRef = React.createRef();

        this.updatePopupId = "updatePopupId";

        this.deleteTodoItem = this.deleteTodoItem.bind(this);
        this.newTodoInputOnClickHandler = this.newTodoInputOnClickHandler.bind(this);
        this.updateTodoItem = this.updateTodoItem.bind(this);
        this.openUpdatePopUpTodoItem = this.openUpdatePopUpTodoItem.bind(this);
    }

    componentDidMount() {
        this.todoRepository.readAll(new Date().getTime()).then(response => {
            this.setState({todoItems: response})
        });
    }

    newTodoInputOnClickHandler() {
        let inputValue = this.newTodoInputRef.current.value;
        this.todoRepository.insert({"todo": inputValue})
            .then((response) => {
                let splittedLocation = response.headers.get("Location").split("/");
                this.todoRepository.read(splittedLocation[splittedLocation.length - 1])
                    .then(value => {
                        this.setState((prevState) => ({
                            todoItems: [...prevState.todoItems, value]
                        }))
                    })
            })
    };

    deleteTodoItem(todoId) {
        this.todoRepository.delete(todoId)
            .then(result => {
                this.setState((prevState) => ({
                    todoItems: prevState.todoItems.filter(todoItem => todoItem.id !== todoId)
                }))
            });
    }

    openUpdatePopUpTodoItem(popupId, todoId, prevTodoText) {
        this.setState({updateItemId: todoId});
        this.updateTodoInputRef.current.value = prevTodoText;
        $("#" + popupId).modal("show")
    }

    updateTodoItem(popupId) {
        this.todoRepository.update(this.state.updateItemId, this.updateTodoInputRef.current.value)
            .then(result => {
                if (result === true) {
                    this.setState((prevState) => ({
                        todoItems: prevState.todoItems.map(todoItem => {
                            if (todoItem.id === this.state.updateItemId) {
                                todoItem.todo = this.updateTodoInputRef.current.value;
                            }

                            return todoItem;
                        })
                    }));
                    $("#" + popupId).modal("hide")
                }
            })
    }

    render() {
        return (
            <div className="container">
                <HeaderRow title="Todo List App"/>

                <div className="row">
                    <div className="col-12">
                        <NewTodoItem buttonText="Insert todo"
                                     newTodoInputRef={this.newTodoInputRef}
                                     newTodoInputOnClickHandler={this.newTodoInputOnClickHandler}/>
                    </div>
                </div>
                <div className="row">
                    <div className="col-12">
                        <hr/>
                    </div>
                </div>
                <div className="row">
                    <div className="col-12">
                        <TodoItemList deleteTodoItem={this.deleteTodoItem}
                                      openUpdatePopUpTodoItem={this.openUpdatePopUpTodoItem}
                                      openUpdatePopUpId={this.updatePopupId}
                                      todoItems={this.state.todoItems}/>
                    </div>
                </div>

                <UpdateTodoItemPopUp updatePopUpTodoItem={this.openUpdatePopUpTodoItem.bind(this, this.updatePopupId)}
                                     updateTodoInputRef={this.updateTodoInputRef}
                                     updateTodoItem={this.updateTodoItem.bind(this, this.updatePopupId)}
                                     modalId={this.updatePopupId}/>
            </div>
        )
    }
}

export default App