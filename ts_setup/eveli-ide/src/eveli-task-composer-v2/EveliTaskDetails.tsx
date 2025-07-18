import React from 'react';
import { Box, Dialog, Divider, Grid2, Stack, Typography } from '@mui/material';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import AttachFileOutlinedIcon from '@mui/icons-material/AttachFileOutlined';
import ThumbUpAltOutlinedIcon from '@mui/icons-material/ThumbUpAltOutlined';
import HistoryIcon from '@mui/icons-material/History';
import NoteAltOutlinedIcon from '@mui/icons-material/NoteAltOutlined';
import dialob_logo from './dialob_logo.svg';

import { DateTime } from 'luxon';

import { TaskCard, TaskCardDataRowText, StartAdornmentIcon, TaskCardDataRowElement } from './TaskCard';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';
import { FormReviewDrawer } from './FormReviewDrawer';
import { TaskCardStyleKey, useTaskCardThemeConfig, taskCardGridSize } from './cardThemeConfig';
import { TaskCardStyleSelect } from './TaskCardStyleSelect';
import { CustomerMessages } from './CustomerMessages';
import { TaskProperties } from './TaskProperties';
import { TaskNotes } from './TaskNotes';
import { TaskAssignee } from './TaskAssignee';
import { TaskRoles } from './TaskRoles';
import { TaskOverdueWarning } from './TaskOverdueWarning';
import { CustomerFeedback } from './CustomerFeedback';
import { EveliTaskStatus } from './EveliTaskStatus';
import { EveliTaskPriority } from './EveliTaskPriority';
import { EveliTaskFiles } from './EveliTaskFiles';
import { EditCustomerMessagesDialog, EditTaskDialog } from '@/eveli-task-composer-edit';
import { TaskCardId } from './types';


export const EveliTaskDetails: React.FC<{ taskId: string }> = (props) => {
  const [task, setTask] = React.useState<TaskApi.Task>();

  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});
  const [reviewOpen, setReviewOpen] = React.useState(false);
  const [flashyCards, setFlashyCards] = React.useState<Record<string, boolean>>({});
  const [editingCardId, setEditingCardId] = React.useState<string | undefined>();

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

  function handleEditDialogOpen(cardId: TaskCardId) {
    setEditingCardId(cardId);
  }

  function handleEditDialogClose() {
    setEditingCardId(undefined);
  }

  return (
    <Grid2 container spacing={style.cardSpacing} m={1}>
      <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
        <TaskCardStyleSelect value={stylePreset} onChange={setStylePreset} />
      </Grid2>

      <Grid2 container size={{ xs: 12, md: reviewOpen ? 6 : 12 }} spacing={style.cardSpacing}
        sx={{
          overflowY: 'auto',
          maxHeight: '100%',
          overflow: 'visible'
        }}>
        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard id='task_main'
            editDialog={editingCardId === 'task_main' && (<EditTaskDialog task={task} open={true} onClose={handleEditDialogClose} />)}
            onDoubleClick={() => handleEditDialogOpen('task_main')}
            isMenu
            title={`Task: ${task.taskRef}`}
            startAdornmentIcon={<StartAdornmentIcon icon={TaskAltIcon} />}
            styleVariant={stylePreset}
            flashy={isCardFlashy('task_main')}
            onToggleFlashy={() => toggleFlashyForCard('task_main')}
            onReview={toggleReview}
          >
            <TaskCardDataRowElement label='Due date' style={style}
              value={
                <Box display='flex' justifyContent='space-between'>
                  {formatAnyDateShort(task.dueDate)}
                  <TaskOverdueWarning task={task} style={style} />
                </Box>
              } />

            <TaskCardDataRowText label='Customer name' value={task.clientIdentificator ? task.clientIdentificator : 'NONE'} style={style} />
            <TaskCardDataRowText label='Subject' value={task.subject} style={style} />
            <TaskCardDataRowText label='Info' value={task.additionalInfo} style={style} />
            <TaskProperties task={task} />
            <Dialog open={false}></Dialog>
          </TaskCard>
        </Grid2>

        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='task-form-summary'
            onReview={toggleReview}
            isMenu
            startAdornmentIcon={<img src={dialob_logo} height='50px' width='80px' style={{ marginRight: 10 }} />}
            styleVariant={stylePreset}
            flashy={isCardFlashy('task-form-summary')}
            onToggleFlashy={() => toggleFlashyForCard('task-form-summary')}
          >
            <TaskCardDataRowElement label='Form name' style={style} value={<Typography sx={style.bodyTypography}>{task.subject}{" "}{'v1.0'}</Typography>} />
            <TaskCardDataRowText label='Submitted' value={formatAnyDateShort(task.created)} style={style} />
            <TaskCardDataRowText label='Can publish feedback?' value='YES' style={style} />
            <TaskCardDataRowText label='Representative?' value='Representative name' style={style} />
            <TaskCardDataRowText label='Other info' value='info here' style={style} />
            <Dialog open={false}></Dialog>
          </TaskCard>
        </Grid2>

        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='status-priority'
            title='Status and Priority'
            isMenu
            startAdornmentIcon={<StartAdornmentIcon icon={TaskAltIcon} />}
            styleVariant={stylePreset}
            flashy={isCardFlashy('status-priority')}
            onToggleFlashy={() => toggleFlashyForCard('status-priority')}
          >
            <Stack direction="column" height="100%">
              <EveliTaskStatus style={style} />
              <Divider sx={{ my: 1 }} />
              <EveliTaskPriority style={style} />
            </Stack>

          </TaskCard>
        </Grid2>

        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='assignees-roles'
            title='Assignees and roles'
            isMenu
            startAdornmentIcon={<StartAdornmentIcon icon={AdminPanelSettingsOutlinedIcon} />}
            styleVariant={stylePreset}
            flashy={isCardFlashy('assignees-roles')}
            onToggleFlashy={() => toggleFlashyForCard('assignees-roles')}
          >
            <TaskCardDataRowElement label='Roles' value={<TaskRoles task={task} />} style={style} />
            <Divider sx={{ my: 1 }} />
            <TaskCardDataRowElement label='Assigned to' value={<TaskAssignee task={task} />} style={style} />
          </TaskCard>
        </Grid2>

        {/*  label={`${comment.userName} ${formatAnyDateShort(comment.created)}`} value={comment.commentText}*/}
        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard id='customer_messages' title='Customer messages'
            editDialog={editingCardId === 'customer_messages' && (<EditCustomerMessagesDialog task={task} open={true} onClose={handleEditDialogClose} />)}
            onDoubleClick={() => handleEditDialogOpen('customer_messages')}
            isMenu
            startAdornmentIcon={<StartAdornmentIcon icon={EditOutlinedIcon} />}
            styleVariant={stylePreset}
            flashy={isCardFlashy('customer_messages')}
            onToggleFlashy={() => toggleFlashyForCard('customer_messages')}
          >
            <Stack direction='column'>
              <CustomerMessages task={task} style={style} />
            </Stack>
          </TaskCard>
        </Grid2>

        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='files'
            title='Files'
            isMenu
            startAdornmentIcon={<StartAdornmentIcon icon={AttachFileOutlinedIcon} />}
            styleVariant={stylePreset}
            flashy={isCardFlashy('files')}
            onToggleFlashy={() => toggleFlashyForCard('files')}
          >
            <EveliTaskFiles task={task} style={style} />
          </TaskCard>
        </Grid2>

        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='feedback'
            title='Customer feedback'
            isMenu
            startAdornmentIcon={<StartAdornmentIcon icon={ThumbUpAltOutlinedIcon} />}
            styleVariant={stylePreset}
            flashy={isCardFlashy('feedback')}
            onToggleFlashy={() => toggleFlashyForCard('feedback')}
          >
            <CustomerFeedback task={task} style={style} />
          </TaskCard>
        </Grid2>


        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='notes'
            title='Notes'
            isMenu
            startAdornmentIcon={<StartAdornmentIcon icon={NoteAltOutlinedIcon} />}
            styleVariant={stylePreset}
            flashy={isCardFlashy('notes')}
            onToggleFlashy={() => toggleFlashyForCard('notes')}
          >
            <TaskNotes task={task} style={style} />
          </TaskCard>
        </Grid2>

        <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
          <TaskCard
            id='task-meta'
            title='History and metadata'
            isMenu
            startAdornmentIcon={<StartAdornmentIcon icon={HistoryIcon} />}
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

  )
}