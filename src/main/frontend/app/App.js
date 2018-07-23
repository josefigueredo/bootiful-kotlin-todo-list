import React from 'react';
import TodoRepository from "./domain/repository/TodoRepository";
import TodoItemList from "./component/TodoItemList";

class App extends React.Component {

    constructor(props){
        super(props);
        this.todoRepository = new TodoRepository();
        this.state = {todoItems: []};
    }

    componentDidMount(){
        this.todoRepository.readAll(new Date().getTime()).then(response => {this.setState({todoItems : response})});
    }

    render(){
        return <TodoItemList todoItems={this.state.todoItems}/>
    }
}

export default App