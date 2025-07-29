import React from 'react';
import { Box, Divider, Grid2, Stack, Typography, useTheme } from '@mui/material';
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

import { TaskCard, TaskCardDataRowText, StartAdornmentIcon, TaskCardDataRowElement } from './TaskCard';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';
import { FormReviewDrawer } from './FormReviewDrawer';
import { TaskCardStyleKey, useTaskCardThemeConfig, taskCardGridSize } from './cardThemeConfig';
import { TaskCardStyleSelect } from './TaskCardStyleSelect';
import { TaskCardId } from './types';

import { TaskEditDialog, TaskOverdueWarning, TaskProperties } from '../eveli-task-composer-v2-task';
import { EveliTaskPriority } from '../eveli-task-composer-v2-priority';
import { EveliTaskStatus } from '../eveli-task-composer-v2-status';
import { NotesEditDialog, NotesTruncated } from '../eveli-task-composer-v2-notes';
import { TaskRolesReadOnly } from '../eveli-task-composer-v2-roles';
import { CustomerMessagesReadOnly, CustomerMessagesEditDialog } from '../eveli-task-composer-v2-messages';
import { CustomerFeedbackEditDialog, CustomerFeedbackReadOnly } from '../eveli-task-composer-v2-feedback';
import { FilesReadOnly, FilesEditDialog } from '../eveli-task-composer-v2-files';
import { TaskAssignee } from '../eveli-task-composer-v2-assignee';



export const EveliTaskDashboard: React.FC<{ taskId: string }> = (props) => {
  const theme = useTheme();
  const [task, setTask] = React.useState<TaskApi.Task>();
  const [reviewOpen, setReviewOpen] = React.useState(false);
  const [flashyCards, setFlashyCards] = React.useState<Record<string, boolean>>({});
  const [editingCardId, setEditingCardId] = React.useState<string | undefined>();
  const [stylePreset, setStylePreset] = React.useState<TaskCardStyleKey>('default');

  const styleConfig = useTaskCardThemeConfig(reviewOpen);
  const style = styleConfig[stylePreset];
  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});

  const initialCardIds = React.useMemo(() => [
    'task_main',
    'task-form-summary',
    'status-priority',
    'assignees-roles',
    'customer_messages',
    'files',
    'feedback',
    'notes',
    'task-meta'
  ], []);

  const [cardOrder, setCardOrder] = React.useState<string[]>(initialCardIds);
  const [draggingCardId, setDraggingCardId] = React.useState<string | null>(null);
  const [dropTargetId, setDropTargetId] = React.useState<string | null>(null);
  const draggedId = React.useRef<string | null>(null);


  function handleDragOver(event: React.DragEvent<HTMLDivElement>) {
    event.preventDefault();
    const id = (event.currentTarget as HTMLElement).id;
    if (id !== draggingCardId) {
      setDropTargetId(id);
    }
  };

  function handleDragStart(event: React.DragEvent<HTMLDivElement>, id: string) {
    draggedId.current = id;
    setDraggingCardId(id);

    const target = event.currentTarget;

    // Clone node and add border to clone
    const clone = target.cloneNode(true) as HTMLElement;
    clone.style.border = `2px dashed ${theme.palette.primary.main}`
    clone.style.height = 'fit-content';
    clone.style.width = 'fit-content';
    clone.style.background = theme.palette.background.default

    document.body.appendChild(clone);

    event.dataTransfer.setDragImage(clone, 10, 10);

    setTimeout(() => {
      document.body.removeChild(clone);
    }, 0);
  };

  function handleDrop(event: React.DragEvent<HTMLDivElement>, id: string) {
    event.preventDefault();

    const fromId = draggedId.current;
    if (!fromId || fromId === id) return;

    const newOrder = [...cardOrder];
    const fromIndex = newOrder.indexOf(fromId);
    const toIndex = newOrder.indexOf(id);

    [newOrder[fromIndex], newOrder[toIndex]] = [newOrder[toIndex], newOrder[fromIndex]];
    setCardOrder(newOrder);

    draggedId.current = null;
    setDraggingCardId(null);


    setDropTargetId(null);
  };

  function handleDragLeave(e: React.DragEvent<HTMLDivElement>, id: string) {
    if (dropTargetId === id) {
      setDropTargetId(null);
    }
  };

  function handleDragEnd() {
    setDraggingCardId(null);
    setDropTargetId(null);
  };

  React.useEffect(() => {
    if (props.taskId && task === undefined) {
      getTask(props.taskId).then(setTask);
    }
  }, [props.taskId, task]);

  if (!task) return null;

  function formatAnyDateShort(value: Date | string | undefined): string {
    if (!value) return '--';
    const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
    return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
  };


  function toggleReview() {
    setReviewOpen(prev => !prev)
  };

  function toggleFlashyForCard(cardId: string) {
    setFlashyCards(prev => ({ ...prev, [cardId]: !prev[cardId] }));
  };

  const isCardFlashy = (cardId: string) => !!flashyCards[cardId];
  const handleEditDialogOpen = (cardId: TaskCardId) => setEditingCardId(cardId);
  const handleEditDialogClose = () => setEditingCardId(undefined);

  const renderCard = (cardId: string) => {
    const commonProps = {
      id: cardId,
      styleVariant: stylePreset,
      flashy: isCardFlashy(cardId),
      onToggleFlashy: () => toggleFlashyForCard(cardId),
      onReview: toggleReview,
      onDragStart: handleDragStart,
      onDragEnd: handleDragEnd,
      onDragOver: handleDragOver,
      onDrop: handleDrop,
      isDragging: draggingCardId === cardId
    };

    console.log(draggingCardId, dropTargetId)

    switch (cardId) {
      case 'task_main':
        return (
          <TaskCard title={`Task: ${task.taskRef}`} {...commonProps} isMenu
            onDragStart={(e) => handleDragStart(e, cardId)}
            onDrop={(e) => handleDrop(e, cardId)}
            onDragLeave={(e) => handleDragLeave(e, cardId)}
            onDragOver={(e) => handleDragOver(e)}
            isDragging={draggingCardId === cardId}
            isDropTarget={dropTargetId === cardId}

            onDoubleClick={() => handleEditDialogOpen('task_main')}
            editDialog={editingCardId === 'task_main' && (<TaskEditDialog task={task} open onClose={handleEditDialogClose} />)}
            startAdornmentIcon={<StartAdornmentIcon icon={TaskAltIcon} />}
          >
            <TaskCardDataRowElement
              label='Due date' style={style}
              value={
                <Box display='flex' justifyContent='space-between'>
                  {formatAnyDateShort(task.dueDate)}
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

      case 'task-form-summary':
        return (
          <TaskCard
            {...commonProps} isMenu startAdornmentIcon={<img src={dialob_logo} height='50px' width='80px' style={{ marginRight: 10 }} />}
            onDragStart={(e) => handleDragStart(e, cardId)}
            onDrop={(e) => handleDrop(e, cardId)}
            onDragLeave={(e) => handleDragLeave(e, cardId)}
            onDragOver={(e) => handleDragOver(e)}
            isDragging={draggingCardId === cardId}
            isDropTarget={dropTargetId === cardId}
          >
            <TaskCardDataRowElement label='Form name' style={style} value={<Typography sx={style.bodyTypography}>{task.subject} v1.0</Typography>} />
            <TaskCardDataRowText label='Submitted' value={formatAnyDateShort(task.created)} style={style} />
            <TaskCardDataRowText label='Can publish feedback?' value='YES' style={style} />
            <TaskCardDataRowText label='Representative?' value='Representative name' style={style} />
            <TaskCardDataRowText label='Other info' value='info here' style={style} />
          </TaskCard>
        );

      case 'status-priority':
        return (
          <TaskCard title='Status and Priority'{...commonProps} isMenu startAdornmentIcon={<StartAdornmentIcon icon={PriorityHighIcon} />}
            onDragStart={(e) => handleDragStart(e, cardId)}
            onDrop={(e) => handleDrop(e, cardId)}
            isDragging={draggingCardId === cardId}
            isDropTarget={dropTargetId === cardId}
            onDragLeave={(e) => handleDragLeave(e, cardId)}
          >
            <Stack direction="column" height="100%">
              <EveliTaskStatus style={style} />
              <Divider sx={{ my: 1 }} />
              <EveliTaskPriority style={style} />
            </Stack>
          </TaskCard>
        );

      case 'assignees-roles':
        return (
          <TaskCard title='Assignees and roles'{...commonProps} isMenu startAdornmentIcon={<StartAdornmentIcon icon={AdminPanelSettingsOutlinedIcon} />}
            onDragStart={(e) => handleDragStart(e, cardId)}
            onDrop={(e) => handleDrop(e, cardId)}
            isDragging={draggingCardId === cardId}
            isDropTarget={dropTargetId === cardId}
            onDragLeave={(e) => handleDragLeave(e, cardId)}
          >
            <TaskCardDataRowElement label='Roles' value={<TaskRolesReadOnly task={task} />} style={style} />
            <Divider sx={{ my: 1 }} />
            <TaskCardDataRowElement label='Assigned to' value={<TaskAssignee task={task} />} style={style} />
          </TaskCard>
        );

      case 'customer_messages':
        return (
          <TaskCard title='Customer messages' {...commonProps} isMenu
            onDragStart={(e) => handleDragStart(e, cardId)}
            onDrop={(e) => handleDrop(e, cardId)}
            isDragging={draggingCardId === cardId}
            isDropTarget={dropTargetId === cardId}
            onDragLeave={(e) => handleDragLeave(e, cardId)}
            startAdornmentIcon={<StartAdornmentIcon icon={EditOutlinedIcon} />}
            onDoubleClick={() => handleEditDialogOpen('customer_messages')}
            editDialog={editingCardId === 'customer_messages' && (<CustomerMessagesEditDialog task={task} open onClose={handleEditDialogClose} />)}
          >
            <CustomerMessagesReadOnly task={task} style={style} />
          </TaskCard>
        );

      case 'files':
        return (
          <TaskCard title='Files' {...commonProps} isMenu
            onDragStart={(e) => handleDragStart(e, cardId)}
            onDrop={(e) => handleDrop(e, cardId)}
            isDragging={draggingCardId === cardId}
            isDropTarget={dropTargetId === cardId}
            onDragLeave={(e) => handleDragLeave(e, cardId)}
            onDoubleClick={() => handleEditDialogOpen('files')}
            startAdornmentIcon={<StartAdornmentIcon icon={AttachFileOutlinedIcon} />}
            editDialog={editingCardId === 'files' && (<FilesEditDialog task={task} open onClose={handleEditDialogClose} />)}
          >
            <FilesReadOnly task={task} style={style} />
          </TaskCard>
        );

      case 'feedback':
        return (
          <TaskCard title='Customer feedback'{...commonProps} isMenu
            onDragStart={(e) => handleDragStart(e, cardId)}
            onDrop={(e) => handleDrop(e, task.id)}
            isDragging={draggingCardId === cardId}
            isDropTarget={dropTargetId === cardId}
            onDragLeave={(e) => handleDragLeave(e, cardId)}
            onDoubleClick={() => handleEditDialogOpen('feedback')}
            startAdornmentIcon={<StartAdornmentIcon icon={ThumbUpAltOutlinedIcon} />}
            editDialog={editingCardId === 'feedback' && (<CustomerFeedbackEditDialog task={task} open onClose={handleEditDialogClose} />)}
          >
            <CustomerFeedbackReadOnly task={task} style={style} />
          </TaskCard>
        );

      case 'notes':
        return (
          <TaskCard title='Notes' {...commonProps} isMenu
            onDragStart={(e) => handleDragStart(e, cardId)}
            onDrop={(e) => handleDrop(e, cardId)}
            isDragging={draggingCardId === cardId}
            isDropTarget={dropTargetId === cardId}
            onDragLeave={(e) => handleDragLeave(e, cardId)}
            onDoubleClick={() => handleEditDialogOpen('notes')}
            startAdornmentIcon={<StartAdornmentIcon icon={NoteAltOutlinedIcon} />}
            editDialog={editingCardId === 'notes' && (<NotesEditDialog task={task} open onClose={handleEditDialogClose} />)}
          >
            <NotesTruncated task={task} style={style} />
          </TaskCard>
        );

      case 'task-meta':
        return (
          <TaskCard title='History and metadata'{...commonProps} isMenu startAdornmentIcon={<StartAdornmentIcon icon={HistoryIcon} />}
            onDragStart={(e) => handleDragStart(e, cardId)}
            onDrop={(e) => handleDrop(e, cardId)}
            isDragging={draggingCardId === cardId}
            isDropTarget={dropTargetId === cardId}
            onDragLeave={(e) => handleDragLeave(e, cardId)}
          >
            <TaskCardDataRowText label='Last edited by' value={task.updaterId} style={style} />
            <TaskCardDataRowText label='Last edited date' value={formatAnyDateShort(task.updated)} style={style} />
          </TaskCard>
        );

      default:
        return null;
    }
  };

  return (
    <Grid2 container spacing={style.cardSpacing} m={1}>
      <Grid2 size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
        <Typography variant='h1'>Edit task: {task.taskRef}</Typography>
        <TaskCardStyleSelect value={stylePreset} onChange={setStylePreset} />
      </Grid2>

      <Grid2 container size={{ xs: 12, md: reviewOpen ? 6 : 12 }} spacing={style.cardSpacing}
        sx={{ overflowY: 'auto', maxHeight: '100%', overflow: 'visible' }}>
        {cardOrder.map((cardId) => (
          <Grid2 key={cardId} size={reviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[stylePreset]}>
            {renderCard(cardId)}
          </Grid2>
        ))}
      </Grid2>

      <FormReviewDrawer onClose={toggleReview} open={reviewOpen} />
    </Grid2>
  );
};
