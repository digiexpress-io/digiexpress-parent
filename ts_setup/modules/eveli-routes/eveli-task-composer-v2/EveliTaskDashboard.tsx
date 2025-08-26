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
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';

import { FormReviewDrawer } from './FormReviewDrawer';
import { TaskEditDialog, TaskOverdueWarning, TaskProperties, TaskPropertiesAlt } from '../eveli-task-composer-v2-task';
import { TaskPriorityReadOnly } from '../eveli-task-composer-v2-priority';
import { TaskStatusReadOnly } from '../eveli-task-composer-v2-status';

import { NotesEditDialog, NotesTruncated } from '../eveli-task-composer-v2-notes';
import { TaskRolesReadOnly } from '../eveli-task-composer-v2-roles';
import { CustomerMessagesReadOnly, CustomerMessagesEditDialog } from '../eveli-task-composer-v2-messages';
import { CustomerFeedbackEditDialog, CustomerFeedbackReadOnly } from '../eveli-task-composer-v2-feedback';
import { FilesReadOnly, FilesEditDialog } from '../eveli-task-composer-v2-files';
import { TaskAssigneeReadOnly } from '../eveli-task-composer-v2-assignee';

import { AssigneeRolesEditDialog } from '../eveli-task-composer-v2-assignee-roles-edit';
import { EveliTaskDashboardContextProvider, useTaskDashboard } from './EveliTaskDashboardContext';
import { PriorityStatusEditDialog } from '../eveli-task-composer-v2-priority-status-edit';

import {
  DraggableCardWrapper, useDragCardController,
  CardConfigContextProvider,
  TaskCardId, useCardConfig, useTaskCardThemeConfig,
  taskCardGridSize,
  TaskCardStyleSelect,
  TaskCard, TaskCardDataRowText, StartAdornmentIcon, TaskCardDataRowElement
} from '../eveli-task-composer-v2-task-card';

import { useFetch } from '@dxs-ts/envir-fetch';
import { TaskApi } from '@dxs-ts/task-api';




export const CardFactory: React.FC<{ cardId: TaskCardId }> = ({ cardId }) => {
  const intl = useIntl();

  const {
    cardTheme, editingCardId, toggleReview,
    isCardFlashy, toggleCardFlashy, isCardAltView, toggleCardAltView, setEditCard,
    isCardExpanded, toggleCardExpanded
  } = useCardConfig();

  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];
  const { task } = useTaskDashboard();
  const [attachments, setAttachments] = React.useState<TaskApi.Attachment[]>([]);
  const { loadAttachments } = useFetch('worker/rest/api/tasks/$taskId/files.GET', {});

  React.useEffect(() => {
    loadAttachments(task.id).then(setAttachments);
  }, [task.id]);




  const commonProps = {
    id: cardId,
    styleVariant: cardTheme,
    flashy: isCardFlashy(cardId),
    onToggleFlashy: () => toggleCardFlashy(cardId),
    altView: isCardAltView(cardId),
    onToggleAltView: () => toggleCardAltView(cardId),
    isExpanded: isCardExpanded(cardId),
    onToggleExpanded: () => toggleCardExpanded(cardId),
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
    case 'task_main_alt':
      return (
        <TaskCard title={`${intl.formatMessage({ id: 'taskcard.title.taskRefId', defaultMessage: 'Task reference id' })}${intl.formatMessage({ id: 'eveli.textSeparatorColon' })} ${task.taskRef}`}
          {...commonProps}
          isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && (<TaskEditDialog open onClose={handleEditClose} />)}
          startAdornmentIcon={<StartAdornmentIcon icon={TaskAltIcon} />}
        >
          <TaskPropertiesAlt style={style} onReview={toggleReview} />
        </TaskCard>
      );
    case 'task_main':
      return (
        <TaskCard title={`${intl.formatMessage({ id: 'taskcard.title.taskRefId', defaultMessage: 'Task reference id' })}${intl.formatMessage({ id: 'eveli.textSeparatorColon' })}${task.taskRef}`}
          {...commonProps}
          isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && (<TaskEditDialog open onClose={handleEditClose} />)}
          startAdornmentIcon={<StartAdornmentIcon icon={TaskAltIcon} />}
          altChildren={<TaskPropertiesAlt style={style} onReview={toggleReview} />}
        >
          <TaskCardDataRowElement label={intl.formatMessage({ id: 'taskcard.body.dueDate', defaultMessage: 'Due date' })} style={style}
            value={
              <Box display='flex' justifyContent='space-between'>
                <Typography sx={{ ...style.bodyTypography }}>{_formatAnyDateShort(task.dueDate)}</Typography>
                <TaskOverdueWarning task={task} style={style} />
              </Box>
            }
          />
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.customerName', defaultMessage: 'Customer name' })} value={task.clientIdentificator || 'NONE'} style={style} />
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.subject', defaultMessage: 'Subject' })} value={task.subject} style={style} />
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.additionalInfo', defaultMessage: 'Extra info' })} value={task.additionalInfo} style={style} />
          <TaskProperties task={task} />
        </TaskCard>
      );

    case 'task_form_summary':
      return (
        <TaskCard {...commonProps} isMenu startAdornmentIcon={<img src={dialob_logo} height='50px' width='80px' style={{ marginRight: 10 }} />}>
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.form.formName', defaultMessage: 'Form name' })} style={style} value={task.subject + " " + "v1.0"} />
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.form.submittedDate', defaultMessage: 'Submitted' })} value={_formatAnyDateShort(task.created)} style={style} />
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.form.canPublishFeedback', defaultMessage: 'Publish feedback?' })} value='YES' style={style} />
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.form.representative', defaultMessage: 'Representative name' })} value='Representative name' style={style} />
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.form.otherInfo', defaultMessage: 'Other info' })} value='info here' style={style} />
        </TaskCard>
      );

    case 'status_priority':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.statusAndPriority', defaultMessage: 'Status and Priority' })}
          {...commonProps}
          isMenu
          onEdit={handleEdit}
          onDoubleClick={handleEdit}
          editDialog={editingCardId === cardId && (<PriorityStatusEditDialog open onClose={handleEditClose} />)}
          startAdornmentIcon={<StartAdornmentIcon icon={PriorityHighIcon} />}>
          <Stack direction="column" height="100%">
            <TaskCardDataRowElement label={intl.formatMessage({ id: 'taskcard.body.task.status', defaultMessage: 'Status' })} value={<TaskStatusReadOnly />} style={style} />
            <Divider sx={{ my: 1 }} />
            <TaskCardDataRowElement label={intl.formatMessage({ id: 'taskcard.body.task.priority', defaultMessage: 'Priority' })} value={<TaskPriorityReadOnly />} style={style} />
          </Stack>
        </TaskCard>
      );

    case 'assignees_roles':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.rolesAndAssignees', defaultMessage: 'Roles and Assignees' })}
          {...commonProps}
          isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && (<AssigneeRolesEditDialog open onClose={handleEditClose} />)}
          startAdornmentIcon={<StartAdornmentIcon icon={AdminPanelSettingsOutlinedIcon} />}>
          <TaskCardDataRowElement label={intl.formatMessage({ id: 'taskcard.body.roles', defaultMessage: 'Roles' })} style={style} value={<TaskRolesReadOnly task={task} style={style} />}
          />
          <Divider sx={{ my: 1 }} />
          <TaskCardDataRowElement label={intl.formatMessage({ id: 'taskcard.body.assignee', defaultMessage: 'Assignee' })} style={style} value={<TaskAssigneeReadOnly task={task} style={style} />} />
        </TaskCard>
      );

    case 'customer_messages':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.customerMessages', defaultMessage: 'Customer messages' })}
          {...commonProps}
          isMenu
          onEdit={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={EditOutlinedIcon} />}
          onDoubleClick={handleEdit}
          editDialog={isEditOpen && (<CustomerMessagesEditDialog open onClose={handleEditClose} />)}
        >
          <CustomerMessagesReadOnly task={task} style={style} />
        </TaskCard>
      );

    case 'files':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.files', defaultMessage: 'Files' })}
          {...commonProps}
          isMenu
          onEdit={handleEdit}
          onDoubleClick={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={AttachFileOutlinedIcon} />}
          editDialog={isEditOpen && (
            <FilesEditDialog open
              task={task}
              onClose={handleEditClose}
              attachments={attachments}
              setAttachments={setAttachments}
            />
          )}
        >
          <FilesReadOnly task={task} style={style} attachments={attachments} />
        </TaskCard>
      );

    case 'feedback':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.customerFeedback', defaultMessage: 'Customer feedback' })}
          {...commonProps}
          isMenu
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
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.notes', defaultMessage: 'Notes' })}
          {...commonProps}
          isMenu
          onEdit={handleEdit}
          onDoubleClick={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={NoteAltOutlinedIcon} />}
          editDialog={isEditOpen && (<NotesEditDialog open onClose={handleEditClose} />)}
        >
          <NotesTruncated task={task} style={style} />
        </TaskCard>
      );

    case 'task_meta':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.history', defaultMessage: 'History and metadata' })}
          {...commonProps}
          isMenu
          startAdornmentIcon={<StartAdornmentIcon icon={HistoryIcon} />}>
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.lastEditedBy', defaultMessage: 'Last edited by' })} value={task.updaterId} style={style} />
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.lastEditedDate', defaultMessage: 'Last edited date' })} value={_formatAnyDateShort(task.updated)} style={style} />
        </TaskCard>
      );

    default:
      return null;
  }
}



const EveliTaskDashboardInternal: React.FC = () => {
  const { cardOrder, isReviewOpen, cardTheme, setCardTheme, toggleReview } = useCardConfig();
  const { getDragPropsForId } = useDragCardController();

  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];
  const { task } = useTaskDashboard();

  return (
    <Grid2 container spacing={style.cardSpacing} m={1}>
      <Grid2 size={isReviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[cardTheme]}>
        <Typography variant='h1'>Edit task: {task.taskRef}</Typography>
        <TaskCardStyleSelect value={cardTheme} onChange={setCardTheme} />
      </Grid2>

      <Grid2 container
        size={{ xs: 12, md: isReviewOpen ? 6 : 12 }}
        spacing={style.cardSpacing}
        sx={{ overflowY: 'auto', maxHeight: '100%', overflow: 'visible' }}>
        {cardOrder.map((cardId) => (
          <Grid2 key={cardId} size={isReviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[cardTheme]}>
            <DraggableCardWrapper {...getDragPropsForId(cardId)}>
              <CardFactory cardId={cardId} />
            </DraggableCardWrapper>
          </Grid2>
        ))}
      </Grid2>

      <FormReviewDrawer onClose={toggleReview} open={isReviewOpen} />
    </Grid2>
  );
};


export const EveliTaskDashboard: React.FC<{ taskId: string }> = (props) => {


  return (
    <EveliTaskDashboardContextProvider taskId={props.taskId}>
      <CardConfigContextProvider>
        <EveliTaskDashboardInternal />
      </CardConfigContextProvider>
    </EveliTaskDashboardContextProvider>

  );
}


function _formatAnyDateShort(value: Date | string | undefined): string {
  if (!value) return '--';
  const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
  return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
};
