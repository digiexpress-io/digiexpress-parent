import React from 'react';
import { Stack } from '@mui/material';
import Burger from 'components-burger';
import Context from 'context';



const TaskCreateActions: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { userId } = Context.useAm()
  const tasks = Context.useTasks();
  const state = Context.useTaskEdit();

  const [open, setOpen] = React.useState(false);


  function handleClose() {
    setOpen(false);
    onClose();
  }

  async function handleCreateAndClose() {
    const createdTask = await tasks.createTask({
      commandType: 'CreateTask',
      title: state.task.title,
      description: state.task.description,
      status: state.task.status,
      priority: state.task.priority,

      startDate: state.task.startDate,
      dueDate: state.task.dueDate,

      roles: state.task.roles,
      assigneeIds: state.task.assignees,
      reporterId: userId,

      labels: state.task.labels,
      extensions: state.task.entry.extensions,
      comments: state.task.comments,
      checklist: state.task.checklist
    });
    handleClose();
  }

  async function handleCreateAndEdit() {
    handleClose();
  }

  return (

    <Stack direction='row' spacing={1}>
      <Burger.SecondaryButton onClick={handleClose} label='core.taskCreate.button.cancel' />
      <Burger.SecondaryButton onClick={handleCreateAndEdit} label='core.taskCreate.button.createAndEdit' />
      <Burger.PrimaryButton onClick={handleCreateAndClose} label='core.taskCreate.button.createAndClose' />
    </Stack>

  );
}


export default TaskCreateActions;