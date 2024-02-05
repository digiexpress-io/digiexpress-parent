import { UserId } from 'descriptor-user-profile';
import { TaskEditEvent, SingleEvent, AssignTaskEventBody, TaskDescriptor, SingleEventDiff } from './descriptor-types';
import { TaskCommand, TaskTransaction } from './backend-types';



export class ImmutableTaskEditEvents {
  private _groups: Record<string, SingleEvent[]>;
  private _previousCommand: Record<string, TaskCommand> = {};

  constructor(init: TaskDescriptor) {
    this._groups = {};
    this.visit(init);
  }

  public build(): readonly TaskEditEvent[] {
    return Object.freeze(this.sort(Object.values(this._groups).map(events => {
      if (events.length === 1) {
        return events[0];
      }
      const targetDate = events[0].targetDate;
      return Object.freeze({ type: "COLLAPSED", items: events, targetDate: targetDate ? new Date(targetDate) : new Date() });
    })));
  }

  private sort(items: TaskEditEvent[]): TaskEditEvent[] {
    return items.sort((b, a) => a.targetDate.getTime() - b.targetDate.getTime());
  }

  private visit(task: TaskDescriptor) {
    task.transactions.forEach(tx => this.visitTransaction(tx, task));
  }

  private visitTransaction(tx: TaskTransaction, task: TaskDescriptor) {
    tx.commands.forEach((command) => this.visitCommand(command, tx, task));
  }

  private visitCommand(command: TaskCommand, tx: TaskTransaction, task: TaskDescriptor) {
    const previous = this._previousCommand[command.commandType];

    let groupId: string = Object.entries(this._groups).length + "";
    if (!this._groups[groupId]) {
      this._groups[groupId] = [];
    }

    this._groups[groupId].push(this.visitEvent(previous, command, tx, task));
    this._previousCommand[command.commandType] = command;
  }


  private visitEvent(previous: TaskCommand | undefined, command: TaskCommand, tx: TaskTransaction, task: TaskDescriptor): SingleEvent {
    const init: SingleEvent = {
      type: 'SINGLE',
      targetDate: command.targetDate ? new Date(command.targetDate) : new Date(),
      body: {
        commandType: command.commandType,
        toCommand: command as any,
        fromCommand: previous as any,
        diff: []
      }
    }

    const diff = this.visitDiff(init);
    init.body.diff = diff;
    return init;
  }

  private visitDiff(init: SingleEvent): SingleEventDiff<any>[] {
    switch (init.body.commandType) {
      case 'AssignTask': diffAssignTask(init.body);
    }
    return [];
  }

}

function diffAssignTask(event: AssignTaskEventBody): SingleEventDiff<UserId>[] {
  const changes = getChanges(event.fromCommand?.assigneeIds, event.toCommand.assigneeIds);
  const result: SingleEventDiff<UserId>[] = [];
  changes.added.forEach(added => result.push({ operation: 'ADDED', value: added, type: undefined }));
  changes.removed.forEach(removed => result.push({ operation: 'REMOVED', value: removed, type: undefined }));
  return result;
}

function getChanges<T>(fromValues: T[] | undefined, toValues: T[]): { added: T[], removed: T[] } {
  const fromArray = Array.isArray(fromValues) ? fromValues : (fromValues ? [fromValues] : []);
  const toArray = Array.isArray(toValues) ? toValues : (toValues ? [toValues] : []);

  const added = toArray.filter(value => !fromArray.includes(value));
  const removed = fromArray.filter(value => !toArray.includes(value));

  return { added, removed };
}
