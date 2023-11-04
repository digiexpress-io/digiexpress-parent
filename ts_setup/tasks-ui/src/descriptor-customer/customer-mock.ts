import { CustomerCaseDescriptor } from './descriptor-types';


export const mockCustomers: CustomerCaseDescriptor[] = [
  {
    id: 'customer-1',
    firstName: 'John',
    lastName: 'Smith',
    ssn: '1234567',
    taskId: 'task-1',
    taskTitle: 'Building permit',
    taskAssigneeIds: ['Lady Sybil Vimes', 'Lord Vetrinari'],
    taskRoles: ['admin-role'],
    taskCreated: new Date('2023-11-04'),
  },
  {
    id: 'customer-1',
    firstName: 'John',
    lastName: 'Smith',
    ssn: '1234567',
    taskId: 'task-10',
    taskTitle: 'School application permit',
    taskAssigneeIds: ['Carrot Ironfoundersson'],
    taskRoles: ['admin-role'],
    taskCreated: new Date('2023-09-30'),
  },
  {
    id: 'customer-2',
    firstName: 'Amy',
    lastName: 'Hollins',
    ssn: 'er40786z',
    taskId: 'task-57',
    taskTitle: 'General message',
    taskAssigneeIds: ['Carrot Ironfoundersson, Lord Vetrinari'],
    taskRoles: ['admin-role'],
    taskCreated: new Date('2023-02-13'),
  },
];
