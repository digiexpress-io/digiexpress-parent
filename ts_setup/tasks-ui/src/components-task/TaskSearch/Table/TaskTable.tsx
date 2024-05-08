import React from 'react';
import { FormattedMessage } from 'react-intl';


import { GroupByTypes, useTaskPrefs, ColumnName, TaskPagination } from '../TableContext';
import { useTitle } from './TableTitle';
import { TaskRow } from './TaskRow';

import { XPagination, XPaper, XPaperTitle, XTable, XTableBody, XTableBodyCell, XTableHeader, XTableRow } from 'components-xtable';
import { XTableHead } from 'components-xtable';


import { AssignTask, AssignTaskRoles, ChangeTaskDueDate, ChangeTaskPriority, ChangeTaskStatus, TaskDescriptor, useTasks } from 'descriptor-task';
import { PrincipalId } from 'descriptor-access-mgmt';

import TaskAssignees from '../../TaskAssignees';
import TaskDueDate from '../../TaskDueDate';
import TaskPriority from '../../TaskPriority';
import TaskRoles from '../../TaskRoles';
import TaskStatus from '../../TaskStatus';
import { TaskRowMenu } from './TaskRowMenu';



export interface DelegateProps {
  groupByType: GroupByTypes,
  groupId: string,
  content: TaskPagination,
  setContent: React.Dispatch<React.SetStateAction<TaskPagination>>
}

export const TaskTable: React.FC<DelegateProps> = ({ groupByType, groupId, content, setContent }) => {
  const tasks = useTasks();
  const prefCtx = useTaskPrefs();
  const title = useTitle({ classifierValue: groupId, groupByType });
  const { pref } = prefCtx;

  const columns: string[] = React.useMemo(() => {
    return pref.visibility.filter(v => v.enabled === false).map(v => v.dataId as ColumnName);
  }, [pref]);

  function handleSorting(key: string, _direction: string) {
    setContent(prev => prev.withOrderBy(key as ColumnName));
  }

  async function handleAssignTask(task: TaskDescriptor, assigneeIds: PrincipalId[]) {
    const command: AssignTask = { assigneeIds, commandType: 'AssignTask', taskId: task.id };
    await tasks.updateActiveTask(task.id, [command]);
  }
  async function handleDueDateChange(task: TaskDescriptor, dueDate: string | undefined) {
    const command: ChangeTaskDueDate = { commandType: 'ChangeTaskDueDate', dueDate, taskId: task.id };
    await tasks.updateActiveTask(task.id, [command]);
  }
  async function handlePriorityChange(task: TaskDescriptor, command: ChangeTaskPriority) {
    await tasks.updateActiveTask(task.id, [command]);
  }
  async function handleAssignRolesChange(task: TaskDescriptor, command: AssignTaskRoles) {
    await tasks.updateActiveTask(task.id, [command]);
  }
  async function handleStatusChange(task: TaskDescriptor, command: ChangeTaskStatus) {
    await tasks.updateActiveTask(task.id, [command]);
  }

  return (<XPaper color={title.color} uuid={`TaskSearch.${groupId}`}>
    <XPaperTitle>
      <FormattedMessage id={'taskSearch.filter.groupedByTitle'} values={{ type: title.title }} />
    </XPaperTitle>

    <XTable columns={7} rows={content.rowsPerPage} hiddenColumns={columns}>
      <XTableHead>
        <XTableRow>
          <XTableHeader onSort={handleSorting} sortable id='title'><FormattedMessage id='tasktable.header.title' /></XTableHeader>
          <XTableHeader onSort={handleSorting} sortable id='assignees'><FormattedMessage id='tasktable.header.assignees' /></XTableHeader>
          <XTableHeader onSort={handleSorting} sortable id='dueDate' defaultSort='asc'><FormattedMessage id='tasktable.header.dueDate' /></XTableHeader>
          <XTableHeader onSort={handleSorting} sortable id='priority'><FormattedMessage id='tasktable.header.priority' /></XTableHeader>
          <XTableHeader onSort={handleSorting} sortable id='roles'><FormattedMessage id='tasktable.header.roles' /></XTableHeader>
          <XTableHeader onSort={handleSorting} sortable id='status'><FormattedMessage id='tasktable.header.status' /></XTableHeader>
          <XTableHeader id='menu'><></></XTableHeader>
        </XTableRow>
      </XTableHead>
      <XTableBody>
        {content.entries.map((row, rowId) => (
          <TaskRow key={row.id} rowId={rowId} row={row} visibleColumns={columns}>
            <XTableBodyCell justifyContent='left' maxWidth={"500px"}>{row.title}</XTableBodyCell>
            <XTableBodyCell><TaskAssignees task={row} onChange={(assigneeIds) => handleAssignTask(row, assigneeIds)} /></XTableBodyCell>
            <XTableBodyCell><TaskDueDate task={row} onChange={(dueDate) => handleDueDateChange(row, dueDate)} /></XTableBodyCell>
            <XTableBodyCell><TaskPriority task={row} onChange={(priority) => handlePriorityChange(row, priority)} /></XTableBodyCell>
            <XTableBodyCell><TaskRoles task={row} onChange={(roles) => handleAssignRolesChange(row, roles)} /></XTableBodyCell>
            <XTableBodyCell width="100px"><TaskStatus task={row} onChange={(status) => handleStatusChange(row, status)} /></XTableBodyCell>
            <XTableBodyCell width="35px" justifyContent='right'><TaskRowMenu row={row} /></XTableBodyCell>
          </TaskRow>))
        }
      </XTableBody>
    </XTable>
    <XPagination state={content} setState={setContent} />
  </XPaper>);
}