import React from 'react';
import { Box, Dialog, Grid2, Stack, Typography } from '@mui/material';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import AttachFileOutlinedIcon from '@mui/icons-material/AttachFileOutlined';
import ThumbUpAltOutlinedIcon from '@mui/icons-material/ThumbUpAltOutlined';
import HistoryIcon from '@mui/icons-material/History';
import NoteAltOutlinedIcon from '@mui/icons-material/NoteAltOutlined';
import dialob_logo from './dialob_logo.svg';

import { DateTime } from 'luxon';

import { TaskCard, TaskCardDataRowText, StartAdornmentIcon } from './TaskCard';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';
import { FormReviewDrawer } from './FormReviewDrawer';
import { TaskCardStyler, TaskCardStyleKey } from './TaskCardStyler';



export const EveliTaskDetails: React.FC<{ taskId: string }> = (props) => {
  const [task, setTask] = React.useState<TaskApi.Task>();
  const [stylePreset, setStylePreset] = React.useState<TaskCardStyleKey>('default');

  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});
  const [reviewOpen, setReviewOpen] = React.useState(false);

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

  function toggleReview() {
    setReviewOpen(prev => !prev)
  }


  return (<>
    <Grid2 container spacing={1} m={2}>
      <Grid2 container size={{ xs: 12, md: reviewOpen ? 6 : 12 }} sx={{ overflowY: 'auto', maxHeight: '100%' }} spacing={1}>

        <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: reviewOpen ? 12 : 12 }}>
          <TaskCardStyler value={stylePreset} onChange={setStylePreset} />
        </Grid2>

        <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: reviewOpen ? 12 : 4 }}>

          <TaskCard id='task-main' title={`Task: ${task.taskRef}`} buttonLabel='Edit' startAdornmentIcon={StartAdornmentIcon(TaskAltIcon)} flashy>
            <TaskCardDataRowText label='Due date' value={formatAnyDateShort(task.dueDate)} />
            <TaskCardDataRowText label='Customer name' value={task.clientIdentificator ? task.clientIdentificator : 'NONE'} />
            <TaskCardDataRowText label='Subject' value={task.subject} />
            <TaskCardDataRowText label='Info' value={task.additionalInfo} />
            <TaskCardDataRowText label='Protected' value='NO' />
            <TaskCardDataRowText label='Source' value='Customer-created' />
            <TaskCardDataRowText label='Type' value='Normal' />
            <Dialog open={false}></Dialog>
          </TaskCard>
        </Grid2>

        <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: reviewOpen ? 12 : 4 }}>
          <TaskCard onClick={toggleReview} id='task-form-summary' buttonLabel='View form' startAdornmentIcon={<img src={dialob_logo} height='50px' width='80px' style={{ marginRight: 10 }} />}>
            <TaskCardDataRowText label='Form name' value={task.subject} />
            <TaskCardDataRowText label='Form version' value='v1.0' />
            <TaskCardDataRowText label='Submitted' value={formatAnyDateShort(task.created)} />
            <TaskCardDataRowText label='Can publish feedback?' value='YES' />
            <TaskCardDataRowText label='Representative?' value='Representative name' />
            <TaskCardDataRowText label='Other info' value='info here' />
            <Dialog open={false}></Dialog>
          </TaskCard>
        </Grid2>

        <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: reviewOpen ? 12 : 4 }}>
          <TaskCard id='assignees-roles' title='Assignees and roles' buttonLabel='Edit' startAdornmentIcon={StartAdornmentIcon(AdminPanelSettingsOutlinedIcon)}>
            <TaskCardDataRowText label='Assignees' value={task.assignedUser ? task.assignedUser : 'Nobody'} />
            <TaskCardDataRowText label='Roles' value={task.assignedRoles ? task.assignedRoles : 'No roles'} />
          </TaskCard>
        </Grid2>


        <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: reviewOpen ? 12 : 4 }}>
          <TaskCard id='customer-messages' title='Customer messages' buttonLabel='New message' startAdornmentIcon={StartAdornmentIcon(EditOutlinedIcon)}>
            <Stack direction='column'>
              {task.comments.length ? task.comments
                .filter(c => c.external === true)
                .slice(0, 3)
                .map((comment) => <TaskCardDataRowText key={comment.id} label={`${comment.userName} ${formatAnyDateShort(comment.created)}`} value={comment.commentText} />
                ) : 'No messages'}
              {task.comments.length > 3 && <Typography variant='caption'>...{task.comments.length - 3} more...</Typography>}
            </Stack>
          </TaskCard>
        </Grid2>

        <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: reviewOpen ? 12 : 4 }}>
          <TaskCard id='files' title='Files' buttonLabel='Upload file' startAdornmentIcon={StartAdornmentIcon(AttachFileOutlinedIcon)}>
            <>No files</>
          </TaskCard>
        </Grid2>

        <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: reviewOpen ? 12 : 4 }} >
          <TaskCard id='feedback' title='Customer feedback' buttonLabel='Edit and publish' startAdornmentIcon={StartAdornmentIcon(ThumbUpAltOutlinedIcon)}>
            <Stack direction='column'>
              <TaskCardDataRowText label='Category' value={task.id} />
              <TaskCardDataRowText label='Subcategory' value={task.id} />
              <TaskCardDataRowText label='Title' value={task.id} />
              <Typography variant='caption'>...more...</Typography>
            </Stack>
          </TaskCard>
        </Grid2>


        <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: reviewOpen ? 12 : 4 }}>
          <TaskCard id='internal-comments' title='Internal comments' buttonLabel='New comment' startAdornmentIcon={StartAdornmentIcon(NoteAltOutlinedIcon)}>
            {task.comments.length ? task.comments.filter(c => !c.external)
              .slice(0, 3)
              .map(comment => <TaskCardDataRowText key={comment.id}
                label={`${comment.userName} ${formatAnyDateShort(comment.created)}`}
                value={comment.commentText}
              />
              ) : 'No comments'}
            <Box flexGrow={1} />
            {task.comments.length > 3 && <Typography variant='caption'>...more...</Typography>}
          </TaskCard>
        </Grid2>

        <Grid2 size={{ xs: 12, sm: 12, md: 12, lg: 12, xl: reviewOpen ? 12 : 4 }}>
          <TaskCard id='task-meta' title='History and metadata' startAdornmentIcon={StartAdornmentIcon(HistoryIcon)}>
            <TaskCardDataRowText label='Last edited by' value={task.updaterId} />
            <TaskCardDataRowText label='Last edited date' value={formatAnyDateShort(task.updated)} />
          </TaskCard>
        </Grid2>
      </Grid2>

      <FormReviewDrawer onClose={toggleReview} open={reviewOpen} />
    </Grid2>
  </>
  )
}