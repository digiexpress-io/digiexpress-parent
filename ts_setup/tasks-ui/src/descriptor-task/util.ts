import { parseISO, isAfter, isEqual, differenceInCalendarDays, differenceInDays } from 'date-fns';

import { UserProfileAndOrg } from 'descriptor-access-mgmt';

import { Task } from './backend-types';
import { AssigneeGroupType, TeamGroupType, _nobody_ } from './descriptor-types';

export function getDaysUntilDue(task: Task, today: Date) {
  const { dueDate } = task;
  if (!dueDate) {
    return undefined;
  }
  const dueDateClean = parseISO(dueDate);
  const daysUntilDue = differenceInDays(dueDateClean, today);

  return daysUntilDue;

}

export function getTeamspaceType(task: Task, profile: UserProfileAndOrg, today: Date): TeamGroupType | undefined {
  if (profile.am.roles.filter((role) => task.roles.includes(role)).length === 0) {
    return undefined;
  }

  const { dueDate } = task;
  const dueDateClean = dueDate ? parseISO(dueDate) : undefined;

  if (dueDateClean && isAfter(today, dueDateClean)) {
    return "groupOverdue";
  }
  if (dueDateClean && differenceInCalendarDays(dueDateClean, today) <= 5) {
    return "groupDueSoon";
  }
  return "groupAvailable";
}


export function getMyWorkType(task: Task, profile: UserProfileAndOrg, today: Date): AssigneeGroupType | undefined {
  if (!task.assigneeIds.includes(profile.userId)) {
    return undefined;
  }

  const { startDate, dueDate } = task;
  const dueDateClean = dueDate ? parseISO(dueDate) : undefined;
  const startDateClean = startDate ? parseISO(startDate) : undefined;

  if (dueDateClean && isAfter(today, dueDateClean) && task.status === 'CREATED') {
    return "assigneeOverdue";
  }
  if (startDateClean && isEqual(startDateClean, today)) {
    return "assigneeStartsToday";
  }
  if (task.status === 'IN_PROGRESS') {
    return "assigneeCurrentlyWorking";
  }

  return "assigneeOther";
}