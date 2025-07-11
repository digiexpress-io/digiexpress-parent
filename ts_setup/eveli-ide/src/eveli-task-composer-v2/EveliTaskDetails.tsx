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
import { TaskCardStyleKey, useTaskCardThemeConfig, taskCardGridSize } from './cardThemeConfig';
import { TaskCardStyleSelect } from './TaskCardStyleSelect';


export const EveliTaskDetails: React.FC<{ taskId: string }> = (props) => {
  const [task, setTask] = React.useState<TaskApi.Task>();

  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});
  const [reviewOpen, setReviewOpen] = React.useState(false);
  const [flashyCards, setFlashyCards] = React.useState<Record<string, boolean>>({});

  const [stylePreset, setStylePreset] = React.useState<TaskCardStyleKey>('default');
  const styleConfig = useTaskCardThemeConfig(reviewOpen);
  const style = styleConfig[stylePreset];

  const toggleFlashyForCard = (cardId: string) => {
    setFlashyCards(prev => ({
      ...prev,
      [cardId]: !prev[cardId],
    }));
  };
  const isCardFlashy = (cardId: string) => !!flashyCards[cardId];

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
      <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
        <TaskCardStyleSelect value={stylePreset} onChange={setStylePreset} />
      </Grid2>

      <Grid2 container size={{ xs: 12, md: reviewOpen ? 6 : 12 }} sx={{ overflowY: 'auto', maxHeight: '100%' }} spacing={style.cardSpacing}>
        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>

          <TaskCard
            id='task-main'
            title={`Task: ${task.taskRef}`}
            buttonLabel='Edit'
            startAdornmentIcon={StartAdornmentIcon(TaskAltIcon)}
            styleVariant={stylePreset}
            flashy={isCardFlashy('task-main')}
            onToggleFlashy={() => toggleFlashyForCard('task-main')}
            onReview={toggleReview}
          >
            <TaskCardDataRowText label='Due date' value={formatAnyDateShort(task.dueDate)} style={style} />
            <TaskCardDataRowText label='Customer name' value={task.clientIdentificator ? task.clientIdentificator : 'NONE'} style={style} />
            <TaskCardDataRowText label='Subject' value={task.subject} style={style} />
            <TaskCardDataRowText label='Info' value={task.additionalInfo} style={style} />
            <TaskCardDataRowText label='Protected' value='NO' style={style} />
            <TaskCardDataRowText label='Source' value='Customer-created' style={style} />
            <TaskCardDataRowText label='Type' value='Normal' style={style} />
            <Dialog open={false}></Dialog>
          </TaskCard>
        </Grid2>

        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard 
            id='task-form-summary'
            onReview={toggleReview}
            buttonLabel='View form'
            startAdornmentIcon={<img src={dialob_logo} height='50px' width='80px' style={{ marginRight: 10 }} />}
            styleVariant={stylePreset}
            flashy={isCardFlashy('task-form-summary')}
            onToggleFlashy={() => toggleFlashyForCard('task-form-summary')}
          >
            <TaskCardDataRowText label='Form name' value={task.subject} style={style} />
            <TaskCardDataRowText label='Form version' value='v1.0' style={style} />
            <TaskCardDataRowText label='Submitted' value={formatAnyDateShort(task.created)} style={style} />
            <TaskCardDataRowText label='Can publish feedback?' value='YES' style={style} />
            <TaskCardDataRowText label='Representative?' value='Representative name' style={style} />
            <TaskCardDataRowText label='Other info' value='info here' style={style} />
            <Dialog open={false}></Dialog>
          </TaskCard>
        </Grid2>

        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='assignees-roles'
            title='Assignees and roles'
            buttonLabel='Edit'
            startAdornmentIcon={StartAdornmentIcon(AdminPanelSettingsOutlinedIcon)}
            styleVariant={stylePreset}
            flashy={isCardFlashy('assignees-roles')}
            onToggleFlashy={() => toggleFlashyForCard('assignees-roles')}
          >
            <TaskCardDataRowText label='Assignees' value={task.assignedUser ? task.assignedUser : 'Nobody'} style={style} />
            <TaskCardDataRowText label='Roles' value={task.assignedRoles ? task.assignedRoles : 'No roles'} style={style} />
          </TaskCard>
        </Grid2>


        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='customer-messages'
            title='Customer messages'
            buttonLabel='New message'
            startAdornmentIcon={StartAdornmentIcon(EditOutlinedIcon)}
            styleVariant={stylePreset}
            flashy={isCardFlashy('customer-messages')}
            onToggleFlashy={() => toggleFlashyForCard('customer-messages')}
          >
            <Stack direction='column'>
              {task.comments.length ? task.comments
                .filter(c => c.external === true)
                .slice(0, 3)
                .map((comment) => <TaskCardDataRowText key={comment.id} label={`${comment.userName} ${formatAnyDateShort(comment.created)}`} value={comment.commentText} style={style} />
                ) : 'No messages'}
              {task.comments.length > 3 && <Typography variant='caption'>...{task.comments.length - 3} more...</Typography>}
            </Stack>
          </TaskCard>
        </Grid2>

        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='files'
            title='Files'
            buttonLabel='Upload file'
            startAdornmentIcon={StartAdornmentIcon(AttachFileOutlinedIcon)}
            styleVariant={stylePreset}
            flashy={isCardFlashy('files')}
            onToggleFlashy={() => toggleFlashyForCard('files')}
          >
            <>No files</>
          </TaskCard>
        </Grid2>

        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='feedback'
            title='Customer feedback'
            buttonLabel='Edit and publish'
            startAdornmentIcon={StartAdornmentIcon(ThumbUpAltOutlinedIcon)}
            styleVariant={stylePreset}
            flashy={isCardFlashy('feedback')}
            onToggleFlashy={() => toggleFlashyForCard('feedback')}
          >
            <Stack direction='column'>
              <TaskCardDataRowText label='Category' value={task.id} style={style} />
              <TaskCardDataRowText label='Subcategory' value={task.id} style={style} />
              <TaskCardDataRowText label='Title' value={task.id} style={style} />
              <Typography variant='caption'>...more...</Typography>
            </Stack>
          </TaskCard>
        </Grid2>


        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='internal-comments'
            title='Internal comments'
            buttonLabel='New comment'
            startAdornmentIcon={StartAdornmentIcon(NoteAltOutlinedIcon)}
            styleVariant={stylePreset}
            flashy={isCardFlashy('internal-comments')}
            onToggleFlashy={() => toggleFlashyForCard('internal-comments')}
          >
            {task.comments.length ? task.comments.filter(c => !c.external)
              .slice(0, 3)
              .map(comment => <TaskCardDataRowText key={comment.id} style={style}
                label={`${comment.userName} ${formatAnyDateShort(comment.created)}`}
                value={comment.commentText}
              />
              ) : 'No comments'}
            <Box flexGrow={1} />
            {task.comments.length > 3 && <Typography variant='caption'>...more...</Typography>}
          </TaskCard>
        </Grid2>

        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='task-meta'
            title='History and metadata'
            startAdornmentIcon={StartAdornmentIcon(HistoryIcon)}
            styleVariant={stylePreset}
            flashy={isCardFlashy('task-meta')}
            onToggleFlashy={() => toggleFlashyForCard('task-meta')}
          >
            <TaskCardDataRowText label='Last edited by' value={task.updaterId} style={style} />
            <TaskCardDataRowText label='Last edited date' value={formatAnyDateShort(task.updated)} style={style} />
          </TaskCard>
        </Grid2>
      </Grid2>

      <FormReviewDrawer onClose={toggleReview} open={reviewOpen} />
    </Grid2>
  </>
  )
}