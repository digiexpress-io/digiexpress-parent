import React from 'react';
import { Box } from '@mui/material';
import SubdirectoryArrowRightIcon from '@mui/icons-material/SubdirectoryArrowRight';
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked';
import { NavigationButton, NavigationSticky } from 'components-generic';
import { moss, cyan } from 'components-colors';
import { Palette, AssigneeGroupType, TaskDescriptor, ChangeTaskPriority, ChangeTaskStatus, ChangeTaskDueDate, AssignTask, useTasks } from 'descriptor-task';

import { XPagination, XPaper, XPaperTitle, XTable, XTableBody, XTableBodyCell, XTableHead, XTableHeader, XTableRow } from 'components-xtable';

import TaskCreateDialog from '../TaskCreate';
import { MyWorkProvider, useMyWork } from './MyWorkContext';
import { FormattedMessage } from 'react-intl';
import { PrincipalId, useAm } from 'descriptor-access-mgmt';
import { useAvatar } from 'descriptor-avatar';
import { TaskRow, TaskRowMenu } from '../TaskTable';
import TaskDueDate from '../TaskDueDate';
import TaskPriority from '../TaskPriority';
import { TaskCustomer } from './TaskCustomer';
import { TaskTitle } from './TaskTitle';

const MyWorkNavigation: React.FC = () => {
  const { setActiveTab, activeTab, getTabItemCount } = useMyWork();
  const { id } = activeTab;
  const [createOpen, setCreateOpen] = React.useState(false);

  function handleTaskCreate() {
    setCreateOpen(prev => !prev)
  }

  function getGroupCount(id: AssigneeGroupType) {
    return { count: getTabItemCount(id) };
  }

  return (<NavigationSticky>
    <NavigationButton id='core.myWork.tab.task.currentlyWorking'
      values={getGroupCount('assigneeCurrentlyWorking')}
      color={Palette.assigneeGroupType.assigneeCurrentlyWorking}
      active={id === 'assigneeCurrentlyWorking'}
      onClick={() => setActiveTab("assigneeCurrentlyWorking")} />

    <NavigationButton id='core.myWork.tab.task.overdue'
      values={getGroupCount('assigneeOverdue')}
      color={Palette.assigneeGroupType.assigneeOverdue}
      active={id === 'assigneeOverdue'}
      onClick={() => setActiveTab("assigneeOverdue")} />

    <NavigationButton id='core.myWork.tab.task.startsToday'
      values={getGroupCount('assigneeStartsToday')}
      color={Palette.assigneeGroupType.assigneeStartsToday}
      active={id === 'assigneeStartsToday'}
      onClick={() => setActiveTab("assigneeStartsToday")} />

    <NavigationButton id='core.myWork.tab.task.available'
      values={getGroupCount('assigneeOther')}
      color={Palette.assigneeGroupType.assigneeOther}
      active={id === 'assigneeOther'}
      onClick={() => setActiveTab("assigneeOther")} />

    <NavigationButton id='core.myWork.tab.recentActivities'
      values={getGroupCount('assigneeOther')}
      color={moss}
      active={id === 'recentActivities'}
      onClick={() => setActiveTab("recentActivities")} />

    <TaskCreateDialog open={createOpen} onClose={handleTaskCreate} />

    <NavigationButton
      id='core.taskCreate.newTask'
      onClick={handleTaskCreate}
      values={undefined}
      active={createOpen}
      color={cyan} />
  </NavigationSticky>);
}

const MyWorkItems: React.FC = () => {
  const tasks = useTasks();
  const { table: content, setTable: setContent } = useMyWork();
  const { iam } = useAm();
  const avatar = useAvatar(iam.id);

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

  return (
    <XPaper uuid={`MyWork.all_tasks`} color={avatar?.colorCode ?? ""}>
      <XPaperTitle>
        <FormattedMessage id='mywork.table.header' />
      </XPaperTitle>

      <XTable columns={6} rows={content.rowsPerPage}>
        <XTableHead>
          <XTableRow>
            <XTableHeader onSort={handleSorting} sortable id='title' colSpan={2}><FormattedMessage id='tasktable.header.title' /></XTableHeader>
            <XTableHeader onSort={handleSorting} sortable id='description'><FormattedMessage id='mywork.table.header.description' /></XTableHeader>
            <XTableHeader onSort={handleSorting} sortable id='dueDate' defaultSort='asc'><FormattedMessage id='tasktable.header.dueDate' /></XTableHeader>
            <XTableHeader onSort={handleSorting} sortable id='priority'><FormattedMessage id='tasktable.header.priority' /></XTableHeader>
          </XTableRow>
        </XTableHead>
        <XTableBody padding={1}>
          {content.entries.map((row, rowId) => (
            <>
              <TaskRow key={row.id + "main"} rowId={rowId} row={row} variant='secondary'>
                <XTableBodyCell id="title" justifyContent='left' maxWidth={"200px"} colSpan={2}><TaskTitle task={row} /></XTableBodyCell>
                <XTableBodyCell id="description" justifyContent='left'>{row.description}</XTableBodyCell>
                <XTableBodyCell id="dueDate"><TaskDueDate task={row} onChange={(dueDate) => handleDueDateChange(row, dueDate)} /></XTableBodyCell>
                <XTableBodyCell id="priority"><TaskPriority task={row} onChange={(priority) => handlePriorityChange(row, priority)} /></XTableBodyCell>
              </TaskRow>
              {row.checklist
                .flatMap(checklist => checklist.items.map(item => ({ checklist, item })))
                .map(({ item, checklist }) => (
                  <TaskRow key={item.id} rowId={rowId} row={row}>
                    <XTableBodyCell id="directory" justifyContent='right' width='50px'><RadioButtonUncheckedIcon /></XTableBodyCell>
                    <XTableBodyCell id="checkListItemTitle" justifyContent='left' colSpan={4}>{item.title}</XTableBodyCell>
                  </TaskRow>
                ))
              }
              <TaskRow key={row.id + "customerId"} rowId={rowId} row={row}>
                <XTableBodyCell id="directory" justifyContent='right' width='50px'><SubdirectoryArrowRightIcon /></XTableBodyCell>
                <XTableBodyCell id="customerId" justifyContent='left' colSpan={4}><TaskCustomer task={row} /></XTableBodyCell>
              </TaskRow>

            </>))
          }
        </XTableBody>
      </XTable>
      <XPagination state={content} setState={setContent} />
    </XPaper>
  )
}

const MyWork: React.FC = () => {
  return <MyWorkProvider>
    <MyWorkNavigation />
    <Box p={1}><MyWorkItems /></Box>
  </MyWorkProvider>;
}

export default MyWork;