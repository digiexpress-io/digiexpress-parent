import { TaskCommand, TaskTransaction, Task } from 'taskclient';
import { TaskEditState, TaskEditMutatorBuilder, TaskEditEvent, SingleEvent } from './task-edit-ctx-types';
import { TaskDescriptor, TaskDescriptorImpl } from 'taskdescriptor';

interface ExtendedInit extends TaskEditState {
  today: Date;
}

class TaskEditStateBuilder implements TaskEditMutatorBuilder {
  private _task: TaskDescriptor;
  private _events: TaskEditEvent[];
  private _today: Date;

  constructor(init: ExtendedInit) {
    this._task = init.task;
    this._today = init.today;
    this._events = new TaskEditEventVisitor({ task: init.task, userId: init.task.profile.userId }).build();
  }
  get today(): Date { return this._today };
  get task(): TaskDescriptor { return this._task };
  get userId(): string { return this._task.profile.userId };
  get events(): TaskEditEvent[] { return this._events }

  withTask(input: Task): TaskEditStateBuilder {
    return new TaskEditStateBuilder({ ...this.clone(), task: new TaskDescriptorImpl(input, this._task.profile, this._today) });
  }
  withTaskDescriptor(input: TaskDescriptor): TaskEditStateBuilder {
    return new TaskEditStateBuilder({ ...this.clone(), task: input });
  }
  withCommands(input: TaskCommand | TaskCommand[]): TaskEditStateBuilder {
    return new TaskEditStateBuilder({ ...this.clone() });
  }
  clone(): ExtendedInit {
    const init = this;
    return {
      today: init.today,
      task: init.task,
      events: init.events
    }
  }
}


class TaskEditEventVisitor {
  private _groups: Record<string, SingleEvent[]>;

  constructor(init: {
    task: TaskDescriptor;
    userId: string;
  }) {
    this._groups = {};
    this.visit(init.task);
  }

  public build(): TaskEditEvent[] {
    return this.sort(Object.values(this._groups).map(events => {
      if (events.length === 1) {
        return events[0];
      }
      const targetDate = events[0].targetDate;
      return { type: "COLLAPSED", items: events, targetDate: targetDate ? new Date(targetDate) : new Date() };
    }));
  }

  sort(items: TaskEditEvent[]): TaskEditEvent[] {
    return items.sort((b, a) => a.targetDate.getTime() - b.targetDate.getTime());
  }

  private visit(task: TaskDescriptor) {
    task.transactions.forEach(tx => this.visitTransaction(tx, task));
  }

  private visitTransaction(tx: TaskTransaction, task: TaskDescriptor) {
    tx.commands.forEach(command => this.visitCommand(command, tx, task));
  }

  private visitCommand(command: TaskCommand, tx: TaskTransaction, task: TaskDescriptor) {
    let groupId: string = Object.entries(this._groups).length + "";
    if (!this._groups[groupId]) {
      this._groups[groupId] = [];
    }

    this._groups[groupId].push(this.visitEvent(command, tx, task));
  }

  private visitEvent(command: TaskCommand, tx: TaskTransaction, task: TaskDescriptor): SingleEvent {
    return {
      type: 'SINGLE',
      targetDate: command.targetDate ? new Date(command.targetDate) : new Date(),
      body: {
        commandType: command.commandType,
        toCommand: command as any,
        fromCommand: undefined

      }

    }
  }
}



export { TaskEditStateBuilder };
export type { };
