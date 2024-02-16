import { Signal, batch, computed, useComputed, useSignal } from "@preact/signals-react";
import { FakeGeneratedService, Todo } from "../lib/generated/FakeGeneratedService";

enum Filter {
    ALL, ACTIVE, COMPLETED
}

function markAllDone(done: boolean) {
    todoSignals.value.forEach((signal) => {
        const todo = signal.value;
        if (todo.done != done) {
            signal.value = { ...todo, done }
        }
    })
}

const todoSignals = FakeGeneratedService.todos();
const todos = todoSignals.items;
const entries = todoSignals.entries;

export default function SharedTodoMVC() {
    const filter = useSignal(Filter.ALL);

    // [todo.id, input.value?]
    const editState = useSignal<[string, string] | [null]>([null]);

    // Could be shared computed() instead but leaving them here to avoid noise in the diff
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
                        todoSignals.insertLast({
                            label: event.currentTarget.value,
                            done: false
                        });
                        event.currentTarget.value = '';
                    }
                }} />
            </div>
            <ul>
                { todoSignals.value.filter(({value: todo}) => {
                    return filter.value == Filter.ALL || (filter.value == Filter.COMPLETED) == todo.done;
                }).map((signal) => {
                    const {key, value: todo} = signal;
                    return <li key={key}>
                        <input type="checkbox" checked={todo.done} onChange={(event) => {
                            signal.value = {...todo, done: event.currentTarget.checked};
                        }} />
                        { editState.value[0] != key
                            ? <span onDoubleClick={() => editState.value = [key, todo.label]}>
                                { todo.label }
                                <button onClick={() => todoSignals.remove(key)}>x</button>
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
                                        signal.value = { ...todo, label: event.currentTarget.value };
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
                    entries.value.forEach(([todo, key]) => todo.done && todoSignals.remove(key));
                }}>Clear completed</button> : null }
            </div>
        </div>
    )
}