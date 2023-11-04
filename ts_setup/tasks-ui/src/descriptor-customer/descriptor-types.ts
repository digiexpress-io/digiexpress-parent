import { TaskId, CustomerId } from 'client';


export interface CustomerCaseDescriptor {
  id: CustomerId,
  firstName: string,
  lastName: string,
  ssn: string,

  taskId: TaskId,
  taskTitle: string,
  taskCreated: Date,
  taskAssigneeIds: string[],
  taskRoles: string[]
}