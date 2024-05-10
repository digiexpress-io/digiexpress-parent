import React from 'react';
import { Button } from '@mui/material';

import { TaskDescriptor } from 'descriptor-task';
import TaskEditDialog from '../TaskEdit';

export const TaskTitle: React.FC<{
  task: TaskDescriptor,
}> = ({ task }) => {
  const [edit, setEdit] = React.useState(false);

  function handleStartEdit() {
    setEdit(true);
  }

  function handleEndEdit() {
    setEdit(false);
  }
  return (
    <>
      <TaskEditDialog open={edit} onClose={handleEndEdit} task={task} />
      {task.title}
    </>
  );
}

