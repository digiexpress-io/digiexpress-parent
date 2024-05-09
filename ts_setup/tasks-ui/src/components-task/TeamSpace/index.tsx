import React from 'react';

import { cyan, orange } from 'components-colors';
import { NavigationButton, NavigationSticky } from 'components-generic';
import { AssignTask, ChangeTaskDueDate, ChangeTaskPriority, ChangeTaskStatus, Palette, TaskDescriptor, TeamGroupType, useTasks } from 'descriptor-task';

import TaskCreateDialog from '../TaskCreate';
import { TeamSpaceProvider, useTeamSpace } from './TeamSpaceContext';
import { XPagination, XPaper, XPaperTitle, XTable, XTableBody, XTableBodyCell, XTableHead, XTableHeader, XTableRow } from 'components-xtable';
import { FormattedMessage } from 'react-intl';
import { TaskCustomer, TaskRow, TaskRowMenu, TaskTAndD } from '../TaskTable';

import TaskAssignees from '../TaskAssignees';
import TaskDueDate from '../TaskDueDate';
import TaskPriority from '../TaskPriority';
import TaskStatus from '../TaskStatus';


import { PrincipalId, useAm } from 'descriptor-access-mgmt';
import { Box } from '@mui/material';



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
      color={cyan} />
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
    return iam.roles.join(", ");
  }, [iam]);

  return (
    <XPaper uuid={`Teamspace.all_tasks`} color={orange}>
      <XPaperTitle>
        <FormattedMessage id='core.teamSpace.title' values={{ myRoles }} />
      </XPaperTitle>

      <XTable columns={6} rows={content.rowsPerPage}>
        <XTableHead>
          <XTableRow>
            <XTableHeader onSort={handleSorting} sortable id='customerId'><FormattedMessage id='tasktable.header.customer' /></XTableHeader>
            <XTableHeader onSort={handleSorting} sortable id='title'><FormattedMessage id='tasktable.header.title' /></XTableHeader>
            <XTableHeader onSort={handleSorting} sortable id='dueDate' defaultSort='asc'>
              <FormattedMessage id='tasktable.header.dueDate' /> \ <FormattedMessage id='tasktable.header.status' />
            </XTableHeader>
            <XTableHeader onSort={handleSorting} sortable id='assignees'><FormattedMessage id='tasktable.header.assignees' /></XTableHeader>
            <XTableHeader onSort={handleSorting} sortable id='priority'><FormattedMessage id='tasktable.header.priority' /></XTableHeader>
            <XTableHeader id='menu'><></></XTableHeader>
          </XTableRow>
        </XTableHead>
        <XTableBody padding={1}>
          {content.entries.map((row, rowId) => (
            <TaskRow key={row.id} rowId={rowId} row={row}>
              <XTableBodyCell justifyContent='left' maxWidth={"200px"} ><TaskCustomer task={row} /></XTableBodyCell>
              <XTableBodyCell justifyContent='left' maxWidth={"300px"}><TaskTAndD task={row} /></XTableBodyCell>
              <XTableBodyCell>
                <Box display="flex" flexDirection="column">
                  <TaskStatus task={row} onChange={(status) => handleStatusChange(row, status)} />
                  <TaskDueDate task={row} onChange={(dueDate) => handleDueDateChange(row, dueDate)} />
                </Box>
              </XTableBodyCell>
              <XTableBodyCell><TaskAssignees task={row} onChange={(assigneeIds) => handleAssignTask(row, assigneeIds)} /></XTableBodyCell>
              <XTableBodyCell><TaskPriority task={row} onChange={(priority) => handlePriorityChange(row, priority)} /></XTableBodyCell>
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
    <Box p={1}>
    <TeamSpaceLayout />
    </Box>
  </TeamSpaceProvider>);
}

export default TeamSpace;