import React from 'react';
import TodoItem from './TodoItem';

export default ({todoItems}) => {
    return <ul>{todoItems.map(item => <TodoItem todoId={item.id} todo={item.todo}/>)}</ul>
}