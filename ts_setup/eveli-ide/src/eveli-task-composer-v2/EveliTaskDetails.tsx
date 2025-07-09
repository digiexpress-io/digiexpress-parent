import React from 'react';
import { Box, Dialog, Grid2, Stack } from '@mui/material';
import TaskIcon from '@mui/icons-material/Task';
import SummarizeIcon from '@mui/icons-material/Summarize';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import EmailIcon from '@mui/icons-material/Email';
import AttachFileOutlinedIcon from '@mui/icons-material/AttachFileOutlined';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import HistoryIcon from '@mui/icons-material/History';
import NoteAltIcon from '@mui/icons-material/NoteAlt';

import { TaskCard, TaskCardDataRowText, StartAdornmentIcon } from './TaskCard';
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


  return (
    <Grid2 container spacing={2} m={2}>
      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 4, xl: 4 }}>
        <TaskCard id='task-main' title={`Task main: ${task.taskRef}`} buttonLabel='Edit' startAdornmentIcon={StartAdornmentIcon(TaskIcon)}>
          <TaskCardDataRowText label='Due date' value={formatAnyDateShort(task.dueDate)} />
          <TaskCardDataRowText label='Customer name' value={task.clientIdentificator ? task.clientIdentificator : 'NONE'} />
          <TaskCardDataRowText label='Subject' value={task.subject} />
          <TaskCardDataRowText label='Info' value={task.additionalInfo} />
          <Dialog open={false}></Dialog>
        </TaskCard>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 4, xl: 4 }} >
        <TaskCard id='task-form-summary' title={`Form summary: ${task.subject}`} buttonLabel='View form' startAdornmentIcon={StartAdornmentIcon(SummarizeIcon)}>
          <TaskCardDataRowText label='Submitted' value={formatAnyDateShort(task.created)} />
          <TaskCardDataRowText label='Can publish feedback?' value='YES' />
          <TaskCardDataRowText label='Representative?' value='Representative name' />
          <TaskCardDataRowText label='Other info' value='info here' />
          <Dialog open={false}></Dialog>
        </TaskCard>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 4, xl: 4 }}>
        <TaskCard id='assignees-roles' title='Assignees and roles' buttonLabel='Edit' startAdornmentIcon={StartAdornmentIcon(AdminPanelSettingsIcon)}>
          <TaskCardDataRowText label='Assignees' value={task.assignedUser ? task.assignedUser : 'Nobody'} />
          <TaskCardDataRowText label='Roles' value={task.assignedRoles ? task.assignedRoles : 'No roles'} />
        </TaskCard>
      </Grid2>


      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 6, xl: 6 }}>
        <TaskCard id='customer-messages' title='Customer messages' buttonLabel='New message' startAdornmentIcon={StartAdornmentIcon(EmailIcon)}>
          <Stack direction='column'>
            {task.comments
              .filter(c => c.external === true)
              .slice(0, 3)
              .map((comment) => <TaskCardDataRowText label={`${comment.userName} ${formatAnyDateShort(comment.created)}`} value={comment.commentText} />
              )}
            {task.comments.length > 3 ? <MoreHorizIcon color='primary' fontSize='small' /> : ''}
          </Stack>
        </TaskCard>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 6, xl: 6 }}>
        <TaskCard id='files' title='Files' buttonLabel='Upload file' startAdornmentIcon={StartAdornmentIcon(AttachFileOutlinedIcon)}>
          <>List of files</>
        </TaskCard>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 4, xl: 4 }} >
        <TaskCard id='feedback' title='Customer feedback' buttonLabel='Edit and publish' startAdornmentIcon={StartAdornmentIcon(ThumbUpIcon)}>
          <Stack direction='column'>
            <TaskCardDataRowText label='Category' value={task.id} />
            <TaskCardDataRowText label='Subcategory' value={task.id} />
            <TaskCardDataRowText label='Title' value={task.id} />
            <MoreHorizIcon color='primary' fontSize='small' />
          </Stack>
        </TaskCard>
      </Grid2>


      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 4, xl: 4 }}>
        <TaskCard id='internal-comments' title='Internal comments' buttonLabel='New comment' startAdornmentIcon={StartAdornmentIcon(NoteAltIcon)}>
          {task.comments.filter(c => !c.external)
            .slice(0, 3)
            .map(comment => <TaskCardDataRowText key={comment.id}
              label={`${comment.userName} ${formatAnyDateShort(comment.created)}`}
              value={comment.commentText}
            />
            )}
          <Box flexGrow={1} />
          {task.comments.length > 3 ? <MoreHorizIcon color='primary' fontSize='small' /> : ''}
        </TaskCard>
      </Grid2>

      <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 4, xl: 4 }}>
        <TaskCard id='task-meta' title='History and metadata' startAdornmentIcon={StartAdornmentIcon(HistoryIcon)}>
          <TaskCardDataRowText label='Last edited by' value={task.updaterId} />
          <TaskCardDataRowText label='Last edited date' value={formatAnyDateShort(task.updated)} />
        </TaskCard>
      </Grid2>
    </Grid2>
  )
}