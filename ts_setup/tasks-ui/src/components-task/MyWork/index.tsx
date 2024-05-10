import React from 'react';
import { Box, Button, ButtonGroup } from '@mui/material';

import { moss, cyan } from 'components-colors';
import { CheckMarkButton, NavigationButton, NavigationSticky, useToggle } from 'components-generic';
import { Palette, AssigneeGroupType, TaskDescriptor, ChangeTaskPriority, ChangeTaskDueDate, useTasks, ChangeChecklistItemDueDate, ChangeChecklistItemAssignees, ChangeChecklistItemCompleted } from 'descriptor-task';
import { XPaper, XPaperTitle, XPaperTitleTypography, XTable, XTableBody, XTableBodyCell, XTableHead, XTableHeader, XTableRow } from 'components-xtable';

import { FormattedMessage } from 'react-intl';

import TaskCreateDialog from '../TaskCreate';
import { MyWorkProvider, useMyWork } from './MyWorkContext';
import { useAvatar } from 'descriptor-avatar';
import { TaskRow } from '../TaskTable';
import TaskDueDate from '../TaskDueDate';
import TaskAssignees from '../TaskAssignees';
import TaskPriority from '../TaskPriority';
import { TaskCustomer } from './TaskCustomer';

import { TaskEditDialog } from '../TaskEdit';
import { CustomerDetailsDialog } from 'components-customer';





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

const MyTask: React.FC<{ task: TaskDescriptor, rowId: number }> = ({ task, rowId }) => {
  const tasks = useTasks();
  const avatar = useAvatar(task.customerId);
  const editTask = useToggle();
  const editCustomer = useToggle();

  async function handleDueDateChange(dueDate: string | undefined) {
    const command: ChangeTaskDueDate = { commandType: 'ChangeTaskDueDate', dueDate, taskId: task.id };
    await tasks.updateActiveTask(task.id, [command]);
  }
  async function handlePriorityChange(command: ChangeTaskPriority) {
    await tasks.updateActiveTask(task.id, [command]);
  }
  async function handleChecklistDueDateChange(dueDate: string | undefined, checklistId: string, checklistItemId: string) {
    const command: ChangeChecklistItemDueDate = { commandType: 'ChangeChecklistItemDueDate', checklistId, checklistItemId, dueDate, taskId: task.id };
    await tasks.updateActiveTask(task.id, [command]);
  }
  async function handleChecklistAssigneesChange(assigneeIds: string[], checklistId: string, checklistItemId: string) {
    const command: ChangeChecklistItemAssignees = { commandType: 'ChangeChecklistItemAssignees', assigneeIds, checklistId, checklistItemId, taskId: task.id };
    await tasks.updateActiveTask(task.id, [command]);
  }
  async function handleChecklistItemCompleted(completed: boolean, checklistId: string, checklistItemId: string) {
    const command: ChangeChecklistItemCompleted = { commandType: 'ChangeChecklistItemCompleted', checklistId, checklistItemId, taskId: task.id, completed };
    await tasks.updateActiveTask(task.id, [command]);
  }


  return (<>
    <TaskEditDialog open={editTask.open} onClose={editTask.handleEnd} task={task} />
    <CustomerDetailsDialog open={editCustomer.open} onClose={editCustomer.handleEnd} customer={task.customerId} />

    <XPaper uuid={`MyWork.all_tasks`} color={avatar?.colorCode ?? ""}>
      <XPaperTitle variant='no-spacing'>
        <ButtonGroup>
          <Button variant='text' onClick={editCustomer.handleStart}>
            <XPaperTitleTypography variant='text-only'>
              <TaskCustomer task={task} />
            </XPaperTitleTypography>
          </Button>
          <Button variant='text' onClick={editTask.handleStart}>
            <XPaperTitleTypography variant='text-only'>{task.title}</XPaperTitleTypography>
          </Button>
        </ButtonGroup>
      </XPaperTitle>
      <XTable columns={6} rows={1}>
        <XTableHead>
          <XTableRow>
            <XTableHeader id='title' colSpan={2}><FormattedMessage id='tasktable.header.title' /></XTableHeader>
            <XTableHeader id='description'><FormattedMessage id='mywork.table.header.description' /></XTableHeader>
            <XTableHeader id='dueDate'><FormattedMessage id='tasktable.header.dueDate' /></XTableHeader>
            <XTableHeader id='priority'><FormattedMessage id='tasktable.header.priority' /></XTableHeader>
          </XTableRow>
        </XTableHead>
        <XTableBody padding={1}>
          <TaskRow key={task.id + "main"} rowId={rowId} row={task}>
            <XTableBodyCell id="title" colSpan={2} justifyContent='left' width="300px">{task.title}</XTableBodyCell>
            <XTableBodyCell id="description" justifyContent='left'>{task.description}</XTableBodyCell>
            <XTableBodyCell id="dueDate"><TaskDueDate task={task} onChange={handleDueDateChange} /></XTableBodyCell>
            <XTableBodyCell id="priority"><TaskPriority task={task} onChange={handlePriorityChange} /></XTableBodyCell>
          </TaskRow>

          {task.checklist
            .flatMap(checklist => checklist.items.map(item => ({ checklist, item })))
            .map(({ item, checklist }) => (
              <TaskRow key={item.id} rowId={rowId} row={task}>
                <XTableBodyCell id="checklist" width='60px'>
                  <CheckMarkButton onClick={() => handleChecklistItemCompleted(!item.completed, checklist.id, item.id)}>{item.completed}</CheckMarkButton>
                </XTableBodyCell>
                <XTableBodyCell id="checkListItemTitle" justifyContent='left'>{checklist.title}</XTableBodyCell>
                <XTableBodyCell id="checkListItemTitle" justifyContent='left'>{item.title}</XTableBodyCell>
                <XTableBodyCell id="dueDate" justifyContent='left'>
                  <TaskDueDate disabled={item.completed} task={{ dueDate: item.dueDate ? new Date(item.dueDate) : undefined }}
                    onChange={(dueDate) => handleChecklistDueDateChange(dueDate, checklist.id, item.id)}
                  />
                </XTableBodyCell>
                <XTableBodyCell id="assignees" justifyContent='left'>
                  <TaskAssignees disabled={item.completed} task={{ assignees: item.assigneeIds }}
                    onChange={(users) => handleChecklistAssigneesChange(users, checklist.id, item.id)}
                  />
                </XTableBodyCell>
              </TaskRow>
            ))
          }
        </XTableBody>
      </XTable>
    </XPaper>
  </>
  )
}

const MyTasks: React.FC = () => {
  const { table: content } = useMyWork();
  return (<>{content.entries.map((task, rowId) => <Box key={rowId} p={1}><MyTask rowId={rowId} task={task} /></Box>)}</>);
}


const MyWork: React.FC = () => {
  return <MyWorkProvider>
    <MyWorkNavigation />
    <MyTasks />
  </MyWorkProvider>;
}

export default MyWork;