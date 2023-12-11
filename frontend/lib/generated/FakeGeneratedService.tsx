import { HelloWorldService } from "Frontend/generated/endpoints";
import { EntryType, EventLog, ListSignal, SignalOptions, ValueSignal } from "../SharedSignals";

export type Position = [number, number];

export interface Todo {
  label: string,
  done: boolean;
}

export class FakeGeneratedService {
  static cursors(options: SignalOptions = {}) {
    const queue = {
      subscribe: HelloWorldService.subscribeCursors,
      publish: HelloWorldService.updateCursors,
    };

    const cursorsQueue = new EventLog<ListSignal<Position, ValueSignal<Position>>>(queue, options, EntryType.LIST);
    return cursorsQueue.getRoot();
  }

  static list() {
    const queue = {
      subscribe: HelloWorldService.subscribeList,
      publish: HelloWorldService.updateList,
    };

    const listLog = new EventLog<ListSignal<string, ValueSignal<string>>>(queue, {}, EntryType.LIST);
    return listLog.getRoot();
  }

  static value(options: SignalOptions = {}) {
    const queue = {
      subscribe: HelloWorldService.subscribeValue,
      publish: HelloWorldService.updateValue,
    };
    
    const valueLog = new EventLog<ValueSignal<string>>(queue, {initialValue: "", ...options}, EntryType.VALUE);
    return valueLog.getRoot();
  }

  static todos() {
    const queue = {
      subscribe: HelloWorldService.subscribeTodos,
      publish: HelloWorldService.updateTodos,
    };
    
    const valueLog = new EventLog<ListSignal<Todo, ValueSignal<Todo>>>(queue, {}, EntryType.LIST);
    return valueLog.getRoot();
  }  
}
