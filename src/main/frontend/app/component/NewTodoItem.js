import React from 'react';

export default ({newTodoInputRef, newTodoInputOnClickHandler}) => {
    return (<div className="input-group mb-3">
        <input className="text-input" ref={newTodoInputRef}/>
        <div className="input-group-append">
            <span className="input-group-text" onClick={newTodoInputOnClickHandler}> Insert todo</span>
        </div>
    </div>)
}