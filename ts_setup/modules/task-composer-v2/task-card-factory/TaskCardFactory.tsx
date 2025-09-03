import React from 'react';
import { Box, Divider, Stack, Typography } from '@mui/material';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import AttachFileOutlinedIcon from '@mui/icons-material/AttachFileOutlined';
import ThumbUpAltOutlinedIcon from '@mui/icons-material/ThumbUpAltOutlined';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';
import PersonSearchOutlinedIcon from '@mui/icons-material/PersonSearchOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import CloudOutlinedIcon from '@mui/icons-material/CloudOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import DriveFileMoveOutlinedIcon from '@mui/icons-material/DriveFileMoveOutlined';
import HistoryIcon from '@mui/icons-material/History';
import NoteAltOutlinedIcon from '@mui/icons-material/NoteAltOutlined';
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';

import { TaskApi, TaskFeature, useTaskBackend } from '@dxs-ts/task-api';

import { TaskRolesReadOnly } from '../task-roles';
import { TaskStatusReadOnly } from '../task-status';
import { TaskAssigneeReadOnly } from '../task-assignee';
import { TaskPriorityReadOnly } from '../task-priority';
import { FilesReadOnly, FilesEditDialog } from '../task-files';
import { NotesEditDialog, NotesTruncated } from '../task-notes';
import { AssigneeRolesEditDialog } from '../task-assignee-roles-edit';
import { PriorityStatusEditDialog } from '../task-priority-status-edit';
import { useTaskDashboard } from '../task-dashboard';
import { CustomerMessagesReadOnly, CustomerMessagesEditDialog } from '../task-messages';
import { CustomerFeedbackEditDialog, CustomerFeedbackReadOnly } from '../task-feedback';
import { TaskTransferEditDialog } from '../task-transfer';
import { TaskEditDialog, TaskOverdueWarning, TaskProperties, TaskPropertiesAlt } from '../task';

import {
  TaskCardId, useCardConfig, useTaskCardThemeConfig,
  TaskCard, TaskCardDataRowText, StartAdornmentIcon, TaskCardDataRowElement
} from '../task-card';




export type FactoryCardId =
  'task_main' |
  'task_main_alt' |
  'task_form_summary' |
  'status_priority' |
  'assignees_roles' |
  'customer_messages' |
  'files' |
  'feedback' |
  'notes' |
  'task_meta' |
  'transfer' |
  'audit_viewers' |
  'audit_commits' |
  'audit_queues' |
  'audit_processes'


export const TASK_CARD_IDS: FactoryCardId[] = [
  'task_main',
  'task_form_summary',
  'status_priority',
  'assignees_roles',
  'customer_messages',
  'files',
  'feedback',
  'notes',
  'task_meta',
  'transfer',
  'audit_viewers',
  'audit_commits',
  'audit_queues',
  'audit_processes'
];

const defaultExpandedCards: FactoryCardId[] = ['task_main_alt', 'assignees_roles', 'status_priority'];



export const TaskCardFactory: React.FC<{ cardId: TaskCardId }> = (initProps) => {
  const intl = useIntl();
  const cardId: FactoryCardId = initProps.cardId as FactoryCardId;

  const {
    cardTheme, editingCardId, toggleReview,
    isCardFlashy, toggleCardFlashy, setEditCard,
    isCardExpanded, toggleCardExpanded, expandedCards
  } = useCardConfig();

  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];

  const { task } = useTaskDashboard();
  const [attachments, setAttachments] = React.useState<TaskApi.Attachment[]>([]);
  const backend = useTaskBackend();


  React.useEffect(() => {
    backend.persistence.findAllAttachments(task.id).then(setAttachments);
  }, [task.id]);

  const commonProps = {
    id: cardId,
    styleVariant: cardTheme,
    isFlashy: isCardFlashy(cardId),
    isExpanded: expandedCards.find(target => target.cardId === cardId) ? isCardExpanded(cardId) : defaultExpandedCards.includes(cardId),
    onToggleFlashy: () => toggleCardFlashy(cardId),
    onToggleExpanded: () => {
      const current = expandedCards.find(target => target.cardId === cardId);
      const isDefault = defaultExpandedCards.includes(cardId)
      if(isDefault) {
        toggleCardExpanded(cardId, current ? undefined : false);
      } else {
        toggleCardExpanded(cardId, current ? undefined : true);
      }
    },
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
          showFlashyToggle={false}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
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

          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
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
        <TaskCard {...commonProps} isMenu
          showFlashyToggle={true}
          showEditOnMenu={false}
          showEditButton={true}
          showReviewOnMenu={true}
        >
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
          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
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
          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
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
        <TaskFeature id='CRM_MESSAGES'>
          <TaskCard title={intl.formatMessage({ id: 'taskcard.title.customerMessages', defaultMessage: 'Customer messages' })}
            {...commonProps}
            isMenu
            showFlashyToggle={true}
            showEditOnMenu={true}
            showEditButton={true}
            showReviewOnMenu={true}
            titleNotifier={task.comments.filter(a => a.external).length}
            onEdit={handleEdit}
            startAdornmentIcon={<StartAdornmentIcon icon={EditOutlinedIcon} />}
            onDoubleClick={handleEdit}
            editDialog={isEditOpen && (<CustomerMessagesEditDialog open onClose={handleEditClose} />)}
          >
            <CustomerMessagesReadOnly task={task} style={style} />
          </TaskCard>
        </TaskFeature>
      );

    case 'files':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.files', defaultMessage: 'Files' })}
          {...commonProps}
          isMenu
          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
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
        <TaskFeature id='TASK_FEEDBACK'>
          <TaskCard title={intl.formatMessage({ id: 'taskcard.title.customerFeedback', defaultMessage: 'Customer feedback' })}
            {...commonProps}
            isMenu
            showFlashyToggle={true}
            showEditOnMenu={true}
            showEditButton={true}
            showReviewOnMenu={false}
            onEdit={handleEdit}
            onDoubleClick={handleEdit}
            startAdornmentIcon={<StartAdornmentIcon icon={ThumbUpAltOutlinedIcon} />}
            editDialog={isEditOpen && (<CustomerFeedbackEditDialog task={task} open onClose={handleEditClose} />)}
          >
            <CustomerFeedbackReadOnly task={task} style={style} />
          </TaskCard>
        </TaskFeature>
      );

    case 'notes':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.notes', defaultMessage: 'Notes' })}
          {...commonProps}
          isMenu
          titleNotifier={task.comments.filter(a => !a.external).length}
          onEdit={handleEdit}
          onDoubleClick={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={NoteAltOutlinedIcon} />}
          editDialog={isEditOpen && (<NotesEditDialog open onClose={handleEditClose} />)}
          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
        >
          <NotesTruncated task={task} style={style} />
        </TaskCard>
      );

    case 'task_meta':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.history', defaultMessage: 'History and metadata' })}
          {...commonProps}
          showFlashyToggle={true}
          showEditOnMenu={false}
          showEditButton={false}
          showReviewOnMenu={false}
          startAdornmentIcon={<StartAdornmentIcon icon={HistoryIcon} />}>
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.lastEditedBy', defaultMessage: 'Last edited by' })} value={task.updaterId} style={style} />
          <TaskCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.lastEditedDate', defaultMessage: 'Last edited date' })} value={_formatAnyDateShort(task.updated)} style={style} />
        </TaskCard>
      );

    case 'transfer':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.transfer', defaultMessage: 'Task transfer' })}
          {...commonProps}
          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={isEditOpen && (<TaskTransferEditDialog open onClose={handleEditClose} task={task} />)}
          isMenu
          startAdornmentIcon={<StartAdornmentIcon icon={DriveFileMoveOutlinedIcon} />}>
          <TaskCardDataRowText
            label={task.transferredId ? (intl.formatMessage({ id: 'taskcard.body.transfer.title', defaultMessage: 'Document title' })
            ) : (
              intl.formatMessage({ id: 'taskcard.body.transfer.none', defaultMessage: 'Not transferred' })
            )}
            value={task.transferredId ?? task.transferredId}
            style={style}
          />
        </TaskCard>
      );

    case 'audit_viewers':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.audit.viewers', defaultMessage: 'Audit: Viewers' })}
          {...commonProps}
          showFlashyToggle={false}
          showEditOnMenu={false}
          showEditButton={false}
          showReviewOnMenu={false}
          startAdornmentIcon={<StartAdornmentIcon icon={PersonSearchOutlinedIcon} />}>
          <>Viewers</>
        </TaskCard>
      );
    case 'audit_commits':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.audit.commits', defaultMessage: 'Audit: Commits' })}
          {...commonProps}
          showFlashyToggle={false}
          showEditOnMenu={false}
          showEditButton={false}
          showReviewOnMenu={false}
          startAdornmentIcon={<StartAdornmentIcon icon={SaveOutlinedIcon} />}>
          <>Commits</>
        </TaskCard>
      );
    case 'audit_queues':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.audit.queues', defaultMessage: 'Audit: Queues' })}
          {...commonProps}
          showFlashyToggle={false}
          showEditOnMenu={false}
          showEditButton={false}
          showReviewOnMenu={false}
          startAdornmentIcon={<StartAdornmentIcon icon={CloudOutlinedIcon} />}>
          <>Queues</>
        </TaskCard>
      );
    case 'audit_processes':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.audit.processes', defaultMessage: 'Audit: Processes' })}
          {...commonProps}
          showFlashyToggle={false}
          showEditOnMenu={false}
          showEditButton={false}
          showReviewOnMenu={false}
          startAdornmentIcon={<StartAdornmentIcon icon={AccountTreeOutlinedIcon} />}>
          <>Processes</>
        </TaskCard>
      );
    default:
      return null;
  }
}

function _formatAnyDateShort(value: Date | string | undefined): string {
  if (!value) return '--';
  const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
  return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
}




