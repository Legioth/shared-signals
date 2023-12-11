import { Signal, batch, useComputed, useSignal } from "@preact/signals-react";

interface Todo {
    id: string,
    label: string,
    done: boolean;
}

enum Filter {
    ALL, ACTIVE, COMPLETED
}

export default function TodoMVC() {
    const todos = useSignal<Todo[]>([]);
    const filter = useSignal(Filter.ALL);

    // [todo.id, input.value?]
    const editState = useSignal<[string, string] | [null]>([null]);

    function updateTodo(updated: Todo) {
        todos.value = todos.value.map(todo => todo.id == updated.id ? updated : todo)
    }
    
    function markAllDone(done: boolean) {
        todos.value = todos.value.map(todo => todo.done != done ? {...todo, done} : todo)
    }        

    const remaining = useComputed(() => todos.value.filter((todo) => !todo.done).length);
    const completed = useComputed(() => todos.value.length - remaining.value );
    const leftLabel = useComputed(() => `${remaining.value} item${remaining.value != 1 ? 's' : ''}`);

    return (
        <div>
            {remaining.value > 0
                ? <button onClick={() => markAllDone(true)}>Complete all</button>
                : completed.value > 0
                    ? <button onClick={() => markAllDone(false)}>Uncomplete all</button>
                    : null}
            <div>
                What needs to be done? <input onKeyUp={(event) => {
                    if (event.key == 'Enter') {
                        todos.value = [...todos.value, {
                            id: crypto.randomUUID(),
                            label: event.currentTarget.value,
                            done: false
                        }];
                        event.currentTarget.value = '';
                    }
                }} />
            </div>
            <ul>
                { todos.value.filter((todo) => {
                    return filter.value == Filter.ALL || (filter.value == Filter.COMPLETED) == todo.done;
                }).map((todo) => {
                    const key = todo.id;
                    return <li key={key}>
                        <input type="checkbox" checked={todo.done} onChange={(event) => {
                            updateTodo({...todo, done: event.currentTarget.checked})
                        }} />
                        { editState.value[0] != key
                            ? <span onDoubleClick={() => editState.value = [key, todo.label]}>
                                { todo.label }
                                <button onClick={() => todos.value = todos.value.filter((t) => t.id != key)}>x</button>
                            </span>
                            : <input autoFocus value={ editState.value[1] }
                                onChange={(event) => editState.value = [key, event.currentTarget.value]}
                                onKeyDown={(event) => {
                                    if (event.key == 'Enter') {
                                        event.currentTarget.blur();
                                    } else if (event.key == 'Escape') {
                                        editState.value = [null]
                                    }
                                }}
                                onBlur={(event) => {
                                    batch(() => {
                                        updateTodo({ ...todo, label: event.currentTarget.value });
                                        editState.value = [null];    
                                    })
                                }} />
                        }
                    </li>;
                })}
            </ul>
            <div hidden={todos.value.length == 0}>
                { leftLabel } left
                <button disabled={filter.value == Filter.ALL} onClick={() => filter.value = Filter.ALL}>All</button>
                <button disabled={filter.value == Filter.ACTIVE} onClick={() => filter.value = Filter.ACTIVE}>Active</button>
                <button disabled={filter.value == Filter.COMPLETED} onClick={() => filter.value = Filter.COMPLETED}>Completed</button>
                { completed.value > 0 ? <button onClick={() => {
                    todos.value = todos.value.filter((todo) => !todo.done);
                }}>Clear completed</button> : null }
            </div>
        </div>
    )
}