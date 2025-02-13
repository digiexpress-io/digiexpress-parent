import { darken, lighten } from '@mui/material';
import { aquamarine, blue, saffron, mandarin } from 'components-colors';

type TaskEventType = 'new-messages' | 'archived-tasks' | 'starts-today-tasks' | 'overdue-tasks' | 'new-tasks' | 'new-checklists-assignment' | 'mentioned-in-comment';
type TaskEventGroup = {
  name: TaskEventGroupName,
  types: TaskEventType[],
  color: string,
  value: number,
  events: TaskEventColored[]
};


type TaskEvent = {
  type: TaskEventType
  value: number,
  
}

type TaskEventColored = TaskEvent & {
  color: string
}


type TaskEventGroupName = "requires-my-attention" | "tasks-starting-today" | "overdue-tasks" | "task-events";



class TaskEventVisitor {
  private _current: Record<TaskEventGroupName, TaskEventGroup>;

  constructor() {
    this._current = {} as any;

  }

  private visitGroups(): TaskEventGroup[] {
    return [
      {
        types: ['new-messages', 'mentioned-in-comment', 'new-checklists-assignment'], name: "requires-my-attention",
        color: blue,
        value: 0,
        events: []
      },

      {
        types: ['starts-today-tasks'], name: "tasks-starting-today",
        color: aquamarine,
        value: 0,
        events: []
      },

      {
        types: ['overdue-tasks'], name: "overdue-tasks",
        color: saffron,
        value: 0,
        events: []
      },

      {
        types: ['archived-tasks', 'new-tasks'], name: "task-events",
        color: mandarin,
        value: 0,
        events: []
      }
    ]
  }

  visit(data: TaskEvent[]): TaskEventGroup[] {
    for (const group of this.visitGroups()) {
      data.forEach(event => this.visitItem(event, group));
    }

    return Object.values(this._current);
  }

  private visitItem(event: TaskEvent, init: TaskEventGroup) {
    if (!this._current[init.name]) {
      this._current[init.name] = init;
    }

    const group = this._current[init.name];
    if (group.types.includes(event.type)) {
      const index = group.types.indexOf(event.type) + 1;
      
      const color = (index % 2 === 0) ? lighten(group.color, 0.15 * index) : darken(group.color, 0.15 * index);
      
      
      group.value = group.value + event.value;
      group.events.push({
        ...event, color
      });
    }
  }
}


function createReport(data: Omit<TaskEvent, "color">[]): {
  groups: TaskEventGroup[],
  events: TaskEventColored[]

} {
  const groups = new TaskEventVisitor().visit(data);
  const events = groups.flatMap(group => group.events);

  return { groups, events };
}

export type { TaskEventType, TaskEventGroup, TaskEvent };
export { createReport }