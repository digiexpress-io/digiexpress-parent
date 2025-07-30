import React from 'react';
import { Box, Divider, Grid2, Stack, Typography } from '@mui/material';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import AttachFileOutlinedIcon from '@mui/icons-material/AttachFileOutlined';
import ThumbUpAltOutlinedIcon from '@mui/icons-material/ThumbUpAltOutlined';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';
import HistoryIcon from '@mui/icons-material/History';
import NoteAltOutlinedIcon from '@mui/icons-material/NoteAltOutlined';
import dialob_logo from './dialob_logo.svg';

import { DateTime } from 'luxon';

import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';
import { FormReviewDrawer } from './FormReviewDrawer';


import { TaskEditDialog, TaskOverdueWarning, TaskProperties } from '../eveli-task-composer-v2-task';
import { TaskPriority } from '../eveli-task-composer-v2-priority';
import { TaskStatus } from '../eveli-task-composer-v2-status';

import { NotesEditDialog, NotesTruncated } from '../eveli-task-composer-v2-notes';
import { TaskRolesReadOnly } from '../eveli-task-composer-v2-roles';
import { CustomerMessagesReadOnly, CustomerMessagesEditDialog } from '../eveli-task-composer-v2-messages';
import { CustomerFeedbackEditDialog, CustomerFeedbackReadOnly } from '../eveli-task-composer-v2-feedback';
import { FilesReadOnly, FilesEditDialog } from '../eveli-task-composer-v2-files';
import { TaskAssignee } from '../eveli-task-composer-v2-assignee';
import {
  DraggableCardWrapper, useDragCardController,
  CardConfigContextProvider,
  TaskCardId, useCardConfig, useTaskCardThemeConfig,
  taskCardGridSize,
  TaskCardStyleSelect,
  TaskCard, TaskCardDataRowText, StartAdornmentIcon, TaskCardDataRowElement
} from '../eveli-task-composer-v2-task-card';



const CardFactory: React.FC<{ cardId: TaskCardId, task: TaskApi.Task }> = ({ cardId, task }) => {
  const { cardTheme, editingCardId, toggleReview, isCardFlashy, toggleCardFlashy, setEditCard } = useCardConfig();
  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];

  const commonProps = {
    id: cardId,
    styleVariant: cardTheme,
    flashy: isCardFlashy(cardId),
    onToggleFlashy: () => toggleCardFlashy(cardId),
    onReview: toggleReview,
  };

  const isEditOpen = cardId === editingCardId;

  function handleEdit() {
    setEditCard(cardId);
  }
  function handleEditClose() {
    setEditCard(undefined);
  }


  switch (cardId) {
    case 'task_main':
      return (
        <TaskCard title={`Task: ${task.taskRef}`} {...commonProps} isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && (<TaskEditDialog task={task} open onClose={handleEditClose} />)}
          startAdornmentIcon={<StartAdornmentIcon icon={TaskAltIcon} />}
        >
          <TaskCardDataRowElement
            label='Due date' style={style}
            value={
              <Box display='flex' justifyContent='space-between'>
                {_formatAnyDateShort(task.dueDate)}
                <TaskOverdueWarning task={task} style={style} />
              </Box>
            }
          />
          <TaskCardDataRowText label='Customer name' value={task.clientIdentificator || 'NONE'} style={style} />
          <TaskCardDataRowText label='Subject' value={task.subject} style={style} />
          <TaskCardDataRowText label='Info' value={task.additionalInfo} style={style} />
          <TaskProperties task={task} />
        </TaskCard>
      );

    case 'task_form_summary':
      return (
        <TaskCard {...commonProps} isMenu startAdornmentIcon={<img src={dialob_logo} height='50px' width='80px' style={{ marginRight: 10 }} />}>
          <TaskCardDataRowElement label='Form name' style={style} value={<Typography sx={style.bodyTypography}>{task.subject} v1.0</Typography>} />
          <TaskCardDataRowText label='Submitted' value={_formatAnyDateShort(task.created)} style={style} />
          <TaskCardDataRowText label='Can publish feedback?' value='YES' style={style} />
          <TaskCardDataRowText label='Representative?' value='Representative name' style={style} />
          <TaskCardDataRowText label='Other info' value='info here' style={style} />
        </TaskCard>
      );

    case 'status_priority':
      return (
        <TaskCard title='Status and Priority'{...commonProps} isMenu startAdornmentIcon={<StartAdornmentIcon icon={PriorityHighIcon} />}>
          <Stack direction="column" height="100%">
            <TaskStatus style={style} />
            <Divider sx={{ my: 1 }} />
            <TaskPriority style={style} />
          </Stack>
        </TaskCard>
      );

    case 'assignees_roles':
      return (
        <TaskCard title='Assignees and roles'{...commonProps} isMenu startAdornmentIcon={<StartAdornmentIcon icon={AdminPanelSettingsOutlinedIcon} />}>
          <TaskCardDataRowElement label='Roles' value={<TaskRolesReadOnly task={task} />} style={style} />
          <Divider sx={{ my: 1 }} />
          <TaskCardDataRowElement label='Assigned to' value={<TaskAssignee task={task} />} style={style} />
        </TaskCard>
      );

    case 'customer_messages':
      return (
        <TaskCard title='Customer messages' {...commonProps} isMenu
          onEdit={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={EditOutlinedIcon} />}
          onDoubleClick={handleEdit}
          editDialog={isEditOpen && (<CustomerMessagesEditDialog task={task} open onClose={handleEditClose} />)}
        >
          <CustomerMessagesReadOnly task={task} style={style} />
        </TaskCard>
      );

    case 'files':
      return (
        <TaskCard title='Files' {...commonProps} isMenu
          onEdit={handleEdit}
          onDoubleClick={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={AttachFileOutlinedIcon} />}
          editDialog={isEditOpen && (<FilesEditDialog task={task} open onClose={handleEditClose} />)}
        >
          <FilesReadOnly task={task} style={style} />
        </TaskCard>
      );

    case 'feedback':
      return (
        <TaskCard title='Customer feedback'{...commonProps} isMenu
          onEdit={handleEdit}
          onDoubleClick={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={ThumbUpAltOutlinedIcon} />}
          editDialog={isEditOpen && (<CustomerFeedbackEditDialog task={task} open onClose={handleEditClose} />)}
        >
          <CustomerFeedbackReadOnly task={task} style={style} />
        </TaskCard>
      );

    case 'notes':
      return (
        <TaskCard title='Notes' {...commonProps} isMenu
          onEdit={handleEdit}
          onDoubleClick={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={NoteAltOutlinedIcon} />}
          editDialog={isEditOpen && (<NotesEditDialog task={task} open onClose={handleEditClose} />)}
        >
          <NotesTruncated task={task} style={style} />
        </TaskCard>
      );

    case 'task_meta':
      return (
        <TaskCard title='History and metadata'{...commonProps} isMenu startAdornmentIcon={<StartAdornmentIcon icon={HistoryIcon} />}>
          <TaskCardDataRowText label='Last edited by' value={task.updaterId} style={style} />
          <TaskCardDataRowText label='Last edited date' value={_formatAnyDateShort(task.updated)} style={style} />
        </TaskCard>
      );

    default:
      return null;
  }
}



const EveliTaskDashboardInternal: React.FC<{ task: TaskApi.Task }> = ({ task }) => {
  const { cardOrder, isReviewOpen, cardTheme, setCardTheme, toggleReview } = useCardConfig();
  const { getDragPropsForId } = useDragCardController();

  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];

  return (
    <Grid2 container spacing={style.cardSpacing} m={1}>
      <Grid2 size={isReviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[cardTheme]}>
        <Typography variant='h1'>Edit task: {task.taskRef}</Typography>
        <TaskCardStyleSelect value={cardTheme} onChange={setCardTheme} />
      </Grid2>

      <Grid2 container size={{ xs: 12, md: isReviewOpen ? 6 : 12 }} spacing={style.cardSpacing}
        sx={{ overflowY: 'auto', maxHeight: '100%', overflow: 'visible' }}>
        {cardOrder.map((cardId) => (
          <Grid2 key={cardId} size={isReviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[cardTheme]}>
            <DraggableCardWrapper {...getDragPropsForId(cardId)}>
              <CardFactory cardId={cardId} task={task} />
            </DraggableCardWrapper>
          </Grid2>
        ))}
      </Grid2>

      <FormReviewDrawer onClose={toggleReview} open={isReviewOpen} />
    </Grid2>
  );
};


export const EveliTaskDashboard: React.FC<{ taskId: string }> = (props) => {
  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});
  const [task, setTask] = React.useState<TaskApi.Task>();

  React.useEffect(() => {
    if (props.taskId && task === undefined) {
      getTask(props.taskId).then(setTask);
    }
  }, [props.taskId, task]);

  if (!task) {
    return null;
  }


  return (
    <CardConfigContextProvider>
      <EveliTaskDashboardInternal task={task} />
    </CardConfigContextProvider>
  );
}


function _formatAnyDateShort(value: Date | string | undefined): string {
  if (!value) return '--';
  const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
  return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
};
