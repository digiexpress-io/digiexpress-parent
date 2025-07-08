import React from 'react';
import { Box, Dialog } from '@mui/material';
import { TaskCard, TaskCardDataRow } from './TaskCard';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';

export const EveliTaskDetails: React.FC<{ taskId: string }> = (props) => {
  const [task, setTask] = React.useState<TaskApi.Task>();
  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});

  React.useEffect(() => {
    if (props.taskId && task === undefined) {
      getTask(props.taskId).then(setTask);
    }
  }, [props.taskId, task]);

  if (!task) {
    return (<></>)
  }

  return (

    <Box p={2} display='flex' gap={1}>

      <TaskCard id='task-main' title='Task main'>
        <TaskCardDataRow label='Due date' value={task.dueDate?.toString()} />
        <TaskCardDataRow label='Customer name' value={task.clientIdentificator ? task.clientIdentificator : 'NONE'} />
        <TaskCardDataRow label='Subject' value={task.subject} />
        <TaskCardDataRow label='Info' value={task.additionalInfo} />
        <Dialog open={false}></Dialog>
      </TaskCard>

      <TaskCard id='task-form-summary' title='Form summary'>
        <TaskCardDataRow label='Submitted' value={task.created?.toString()} />
        <TaskCardDataRow label='Can publish feedback?' value='YES' />
        <TaskCardDataRow label='Representative?' value='Representative name' />
        <TaskCardDataRow label='Other info' value='info here' />
        <Dialog open={false}></Dialog>
      </TaskCard>

    </Box>
  )
}