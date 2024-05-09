import React from 'react';

import { cyan } from 'components-colors';
import { NavigationButton, NavigationSticky  } from 'components-generic';
import { AssignTask, ChangeTaskDueDate, ChangeTaskPriority, ChangeTaskStatus, Palette, TaskDescriptor, TeamGroupType, useTasks } from 'descriptor-task';

import TaskCreateDialog from '../TaskCreate';
import { TeamSpaceProvider, useTeamSpace } from './TeamSpaceContext';
import { XPagination, XPaper, XPaperTitle, XTable, XTableBody, XTableBodyCell, XTableHead, XTableHeader, XTableRow } from 'components-xtable';
import { FormattedMessage } from 'react-intl';
import { TaskRow, TaskRowMenu } from '../TaskTable';

import TaskAssignees from '../TaskAssignees';
import TaskDueDate from '../TaskDueDate';
import TaskPriority from '../TaskPriority';
import TaskStatus from '../TaskStatus';


import { PrincipalId, useAm } from 'descriptor-access-mgmt';



const TeamSpaceNavigation: React.FC = () => {
  const { setActiveTab, activeTab, getTabItemCount } = useTeamSpace();
  const { id } = activeTab;
  const [createOpen, setCreateOpen] = React.useState(false);

  function handleTaskCreate() {
    setCreateOpen(prev => !prev)
  }

  function getGroupCount(id: TeamGroupType) {
    return { count: getTabItemCount(id) };
  }

  return (<NavigationSticky>
      
    <NavigationButton id='core.teamSpace.tab.task.overdue' 
      values={getGroupCount('groupOverdue')} 
      color={Palette.teamGroupType.groupOverdue}
      active={id === 'groupOverdue'}
      onClick={() => setActiveTab("groupOverdue")} />

    <NavigationButton id='core.teamSpace.tab.task.dueSoon' 
      values={getGroupCount('groupDueSoon')} 
      color={Palette.teamGroupType.groupDueSoon}
      active={id === 'groupDueSoon'}
      onClick={() => setActiveTab("groupDueSoon")} />

    <NavigationButton id='core.teamSpace.tab.task.available' 
      values={getGroupCount('groupAvailable')} 
      color={Palette.teamGroupType.groupAvailable}
      active={id === 'groupAvailable'}
      onClick={() => setActiveTab("groupAvailable")} />

    <TaskCreateDialog open={createOpen} onClose={handleTaskCreate} />

    <NavigationButton
      id='core.taskCreate.newTask'
      onClick={handleTaskCreate}
      values={undefined}
      active={createOpen}
      color={cyan}/>
  </NavigationSticky>);
}


const TeamSpaceLayout: React.FC = () => {
  const tasks = useTasks();
  const { table: content, setTable: setContent } = useTeamSpace();
  const { iam } = useAm();


  function handleSorting(key: string, _direction: string) {
    const column: keyof TaskDescriptor = key as any;
    setContent(prev => prev.withOrderBy(column));
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
  async function handleStatusChange(task: TaskDescriptor, command: ChangeTaskStatus) {
    await tasks.updateActiveTask(task.id, [command]);
  }

  const myRoles = React.useMemo(() => {
    return "TODO:::";
  },[iam]);

  return (
    <XPaper uuid={`Teamspace.all_tasks`}>
    <XPaperTitle>
      <FormattedMessage id='core.teamSpace.title' values={{myRoles}} />
    </XPaperTitle>

    <XTable columns={7} rows={content.rowsPerPage}>
      <XTableHead>
        <XTableRow>
          <XTableHeader onSort={handleSorting} sortable id='title'><FormattedMessage id='tasktable.header.title' /></XTableHeader>
          <XTableHeader onSort={handleSorting} sortable id='assignees'><FormattedMessage id='tasktable.header.assignees' /></XTableHeader>
          <XTableHeader onSort={handleSorting} sortable id='dueDate' defaultSort='asc'><FormattedMessage id='tasktable.header.dueDate' /></XTableHeader>
          <XTableHeader onSort={handleSorting} sortable id='priority'><FormattedMessage id='tasktable.header.priority' /></XTableHeader>
          <XTableHeader onSort={handleSorting} sortable id='status'><FormattedMessage id='tasktable.header.status' /></XTableHeader>
          <XTableHeader id='menu'><></></XTableHeader>
        </XTableRow>
      </XTableHead>
      <XTableBody>
        {content.entries.map((row, rowId) => (
          <TaskRow key={row.id} rowId={rowId} row={row}>
            <XTableBodyCell justifyContent='left' maxWidth={"500px"}>{row.title}</XTableBodyCell>
            <XTableBodyCell><TaskAssignees task={row} onChange={(assigneeIds) => handleAssignTask(row, assigneeIds)} /></XTableBodyCell>
            <XTableBodyCell><TaskDueDate task={row} onChange={(dueDate) => handleDueDateChange(row, dueDate)} /></XTableBodyCell>
            <XTableBodyCell><TaskPriority task={row} onChange={(priority) => handlePriorityChange(row, priority)} /></XTableBodyCell>
            <XTableBodyCell width="100px"><TaskStatus task={row} onChange={(status) => handleStatusChange(row, status)} /></XTableBodyCell>
            <XTableBodyCell width="35px" justifyContent='right'><TaskRowMenu row={row} /></XTableBodyCell>
          </TaskRow>))
        }
      </XTableBody>
    </XTable>
    <XPagination state={content} setState={setContent} />
  </XPaper>
  )
}

const TeamSpace: React.FC = () => {
  return (<TeamSpaceProvider>
    <TeamSpaceNavigation />
    <TeamSpaceLayout />
  </TeamSpaceProvider>);
}

export default TeamSpace;