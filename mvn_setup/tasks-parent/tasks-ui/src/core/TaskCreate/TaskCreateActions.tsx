import React from 'react';
import { Stack } from '@mui/material';
import Burger from '@the-wrench-io/react-burger';
import Context from 'context';



const TaskCreateActions: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const backend = Context.useBackend();
  const org = Context.useOrg();
  const tasks = Context.useTasks();
  const { state } = Context.useTaskEdit();

  const [open, setOpen] = React.useState(false);


  function handleClose() {
    setOpen(false);
    onClose();
  }

  async function handleCreateAndClose() {
    const createdTask = await backend.task.createTask({
      commandType: 'CreateTask',
      title: state.task.title,
      description: state.task.description,
      status: state.task.status,
      priority: state.task.priority,

      startDate: state.task.startDate,
      dueDate: state.task.dueDate,

      roles: state.task.roles,
      assigneeIds: state.task.assignees,
      reporterId: org.state.iam.userId,

      labels: state.task.labels,
      extensions: state.task.entry.extensions,
      comments: state.task.comments,
      checklist: state.task.checklist
    });
    await tasks.reload()
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