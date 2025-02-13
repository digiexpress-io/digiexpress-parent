import { TaskEvent } from './reporting-types';




function gen(): TaskEvent[] {
  
  
  return [
    { type: 'archived-tasks', value: 10 },
    { type: 'mentioned-in-comment', value: 35 },
    { type: 'new-messages', value: 5 },
    { type: 'starts-today-tasks', value: 2 },
    { type: 'new-checklists-assignment', value: 0 },
    { type: 'new-tasks', value: 9 },
    { type: 'overdue-tasks', value: 1 },
    
  ];
}


export default gen;