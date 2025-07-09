import React from 'react';
import { Box, Dialog, Grid2 } from '@mui/material';
import { TaskCard, TaskCardDataRowText, TaskCardDataRowElement } from './TaskCard';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';
import { DateTime } from 'luxon';

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

  const formatAnyDateShort = (value: Date | string | undefined): string => {
    if (!value) {
      return '--';
    }

    const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
    return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
  };


  console.log(task.created)
  return (
    <Grid2 container spacing={2}>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 4, xl: 4 }}>
        <TaskCard id='task-main' title='Task main' buttonLabel='Edit'>
          <TaskCardDataRowText label='Due date' value={formatAnyDateShort(task.dueDate)} />
          <TaskCardDataRowText label='Customer name' value={task.clientIdentificator ? task.clientIdentificator : 'NONE'} />
          <TaskCardDataRowText label='Subject' value={task.subject} />
          <TaskCardDataRowText label='Info' value={task.additionalInfo} />
          <Dialog open={false}></Dialog>
        </TaskCard>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 4, xl: 4 }}>
        <TaskCard id='task-form-summary' title='Form summary' buttonLabel='View form'>
          <TaskCardDataRowText label='Submitted' value={formatAnyDateShort(task.created)} />
          <TaskCardDataRowText label='Can publish feedback?' value='YES' />
          <TaskCardDataRowText label='Representative?' value='Representative name' />
          <TaskCardDataRowText label='Other info' value='info here' />
          <Dialog open={false}></Dialog>
        </TaskCard>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 4, xl: 4 }}>
        <TaskCard id='assignees-roles' title='Assignees and roles'>
          <TaskCardDataRowElement label='Assignees:' value={<>assignees</>} />
          <TaskCardDataRowElement label='Roles:' value={<>roles</>} />
        </TaskCard>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 6, xl: 6 }}>
        <TaskCard id='customer-messages' title='Customer messages'>
          <>Customer messages</>
        </TaskCard>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 6, xl: 6 }}>
        <TaskCard id='files' title='Files'>
          <>List of files</>
        </TaskCard>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12 }}>
        <TaskCard id='feedback' title='Customer feedback'>
          <>Feedback</>
        </TaskCard>
      </Grid2>


      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 4, xl: 4 }}>
        <TaskCard id='internal-comments' title='Internal comments'>
          <>Internal comments</>
        </TaskCard>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 4, xl: 4 }}>
        <TaskCard id='task-meta' title='History and metadata'>
          <TaskCardDataRowText label='Last edited by:' value='John Smith' />
          <TaskCardDataRowText label='Last edited date:' value={formatAnyDateShort(task.updated)} />
        </TaskCard>
      </Grid2>
    </Grid2>
  )
}