import React from 'react';
import { useIntl } from 'react-intl';

import { TextField, Box, IconButton, } from '@mui/material';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import CircleNotificationsOutlinedIcon from '@mui/icons-material/CircleNotificationsOutlined';
import CloseIcon from '@mui/icons-material/Close';

import Client from 'client';
import Context from 'context';
import { 
  ChangeTaskInfo, 
  TaskUpdateCommand, ChangeTaskStatus,
  AssignTask, AssignTaskRoles, ChangeTaskPriority,
  ChangeTaskStartDate, ChangeTaskDueDate
} from 'descriptor-task';

import TaskChecklist from '../TaskChecklist';
import TaskAssignees from '../TaskAssignees';
import TaskStatus from '../TaskStatus';
import TaskPriority from '../TaskPriority';
import TaskRoles from '../TaskRoles';
import TaskStartDate from '../TaskStartDate';
import TaskDueDate from '../TaskDueDate';

const Title: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();
  const backend = Context.useTasks();

  const intl = useIntl();
  const [title, setTitle] = React.useState(ctx.task.title);

  function handleTitleChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTitle(event.target.value);
  }

  async function handleChange() {
    const command: ChangeTaskInfo = {
      commandType: 'ChangeTaskInfo',
      taskId: ctx.task.id,
      description: ctx.task.description,
      title
    };
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'core.taskEdit.taskTitle' })}
    fullWidth
    value={title}
    onChange={handleTitleChange}
    onBlur={handleChange}
  />);
}

const Description: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();
  const [description, setDescription] = React.useState(ctx.task.description);
  const backend = Context.useTasks();
  const intl = useIntl();

  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setDescription(event.target.value);
  }

  async function handleChange() {
    const command: ChangeTaskInfo = {
      commandType: 'ChangeTaskInfo',
      taskId: ctx.task.id,
      title: ctx.task.title,
      description
    };
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    placeholder={intl.formatMessage({ id: 'core.taskEdit.taskDescription' })}
    fullWidth multiline minRows={2} maxRows={8}
    value={description}
    onChange={handleDescriptionChange}
    onBlur={handleChange}
  />);
}

const Checklist: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();
  const backend = Context.useTasks();

  async function handleChange(commands: TaskUpdateCommand<any>[]) {
    const updatedTask = await backend.updateActiveTask(ctx.task.id, commands);
    ctx.withTask(updatedTask);
  }

  return (<TaskChecklist onChange={handleChange} />
  )
}

const Status: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();
  const backend = Context.useTasks();

  async function handleStatusChange(command: ChangeTaskStatus) {
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  return (
    <TaskStatus task={ctx.task} onChange={handleStatusChange} />
  )
}

const Assignee: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();
  const backend = Context.useTasks();

  async function handleAssigneeChange(assigneeIds: Client.UserId[]) {
    const command: AssignTask = { assigneeIds, commandType: 'AssignTask', taskId: ctx.task.id };
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  return (
    <TaskAssignees task={ctx.task} onChange={handleAssigneeChange} fullnames />
  )
}

const Roles: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();
  const backend = Context.useTasks();

  async function handleRolesChange(command: AssignTaskRoles) {
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  return (<TaskRoles task={ctx.task} onChange={handleRolesChange} fullnames />)
}

const Priority: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();
  const backend = Context.useTasks();

  async function handlePriorityChange(command: ChangeTaskPriority) {
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  return (
    <TaskPriority task={ctx.task} priorityTextEnabled onChange={handlePriorityChange} />
  )
}

const StartDate: React.FC = () => {
  const ctx = Context.useTaskEdit();
  const backend = Context.useTasks();

  async function handleStartDateChange(command: ChangeTaskStartDate) {
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  return (<TaskStartDate task={ctx.task} onChange={handleStartDateChange} />);
}

const DueDate: React.FC = () => {
  const ctx = Context.useTaskEdit();
  const backend = Context.useTasks();

  async function handleDueDateChange(dueDate: string | undefined) {
    const command: ChangeTaskDueDate = {
      commandType: 'ChangeTaskDueDate',
      dueDate,
      taskId: ctx.task.id
    };

    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  return (<TaskDueDate task={ctx.task} onChange={handleDueDateChange} />);
}

const MessageCount: React.FC<{}> = () => {
  return (<Box display='flex' alignItems='center'><MailOutlineIcon />4</Box>)
}

const AttachmentCount: React.FC<{}> = () => {
  return (<Box display='flex' alignItems='center'><AttachFileIcon />2</Box>)
}

const NewItemNotification: React.FC<{}> = () => {
  return (<Box display='flex' alignItems='center'><CircleNotificationsOutlinedIcon />3</Box>)
}

const CloseDialogButton: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <IconButton onClick={onClose}>
      <CloseIcon />
    </IconButton>
  )
}

const Fields = {
  Title, Description, Checklist, Status, Assignee, Priority, StartDate, DueDate, MessageCount, AttachmentCount,
  NewItemNotification, CloseDialogButton, Roles
}
export default Fields;
