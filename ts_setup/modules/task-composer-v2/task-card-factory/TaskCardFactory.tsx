import React from 'react';
import { Box, Divider, Stack, Typography } from '@mui/material';
import { TaskAlt as TaskAltIcon } from '@mui/icons-material';
import { AdminPanelSettingsOutlined as AdminPanelSettingsOutlinedIcon } from '@mui/icons-material';
import { EditOutlined as EditOutlinedIcon } from '@mui/icons-material';
import { AttachFileOutlined as AttachFileOutlinedIcon } from '@mui/icons-material';
import { ThumbUpAltOutlined as ThumbUpAltOutlinedIcon } from '@mui/icons-material';
import { PriorityHigh as PriorityHighIcon } from '@mui/icons-material';
import { PersonSearchOutlined as PersonSearchOutlinedIcon } from '@mui/icons-material';
import { AssignmentIndOutlined as AssignmentIndOutlinedIcon } from '@mui/icons-material';
import { SaveOutlined as SaveOutlinedIcon } from '@mui/icons-material';
import { CloudOutlined as CloudOutlinedIcon } from '@mui/icons-material';
import { AccountTreeOutlined as AccountTreeOutlinedIcon } from '@mui/icons-material';
import { DriveFileMoveOutlined as DriveFileMoveOutlinedIcon } from '@mui/icons-material';
import { History as HistoryIcon } from '@mui/icons-material';
import { NoteAltOutlined as NoteAltOutlinedIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';

import { TaskApi, TaskFeature } from '@dxs-ts/task-api';

import { TaskRolesReadOnly } from '../task-roles';
import { TaskStatusReadOnly } from '../task-status';
import { TaskAssigneeReadOnly } from '../task-assignee';
import { TaskPriorityReadOnly } from '../task-priority';
import { FilesReadOnly, FilesEditDialog } from '../task-files';
import { NotesEditDialog, NotesTruncated } from '../task-notes';
import { TaskAssignmentEditDialog, TaskAssignmentReadOnly } from '../task-assignable';
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
import { TaskAuditViewersTable } from '../task-audit-viewers';
import { TaskAuditCommitsTable } from '../task-audit-commits';
import { TaskAuditProcessesTable } from '../task-audit-processes';
import { TaskAuditFlow } from '../task-audit-flow';
import { TaskAuditQueueMessagesTable } from '../task-audit-queue-messages';
import { TaskAuditQueueBindingsTable } from '../task-audit-queue-bindings';
import { TaskAuditQueueDeliveriesTable } from '../task-audit-queue-deliveries';
import { TaskAuditQueuesTable } from '../task-audit-queue';
import { FeedbackApi, useFeedback } from '@dxs-ts/task-feedback';



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
  'assignable' |
  'audit_viewers' |
  'audit_commits' |
  'audit_queues' |
  'audit_queue_bindings' |
  'audit_queue_deliveries' |
  'audit_processes' |
  'audit_flow' |
  'audit_queue_messages'


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
  'assignable',
  'audit_viewers',
  'audit_commits',
  'audit_queues',
  'audit_queue_bindings',
  'audit_queue_deliveries',
  'audit_processes',
  'audit_flow',
  'audit_queue_messages'
];

const defaultExpandedCards: FactoryCardId[] = ['task_main_alt', 'assignees_roles', 'status_priority'];



export const TaskCardFactory: React.FC<{ cardId: TaskCardId }> = (initProps) => {
  const intl = useIntl();
  const cardId: FactoryCardId = initProps.cardId as FactoryCardId;
  const { task } = useTaskDashboard();
  
  const {
    cardTheme, editingCardId, toggleReview,
    isCardFlashy, toggleCardFlashy, setEditCard,
    isCardExpanded, toggleCardExpanded, expandedCards
  } = useCardConfig();

  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];


  const commonProps = {
    id: cardId,
    styleVariant: cardTheme,
    isFlashy: isCardFlashy(cardId),
    isExpanded: expandedCards.find(target => target.cardId === cardId) ? isCardExpanded(cardId) : defaultExpandedCards.includes(cardId),
    onToggleFlashy: () => toggleCardFlashy(cardId),
    onToggleExpanded: () => {
      const current = expandedCards.find(target => target.cardId === cardId);
      const isDefault = defaultExpandedCards.includes(cardId)
      if (isDefault) {
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

    case 'assignable':
      return (
        <TaskFeature id='ASSIGNABLE'>
          <TaskCard title={intl.formatMessage({ id: 'taskcard.title.assignable', defaultMessage: 'Assigned to customer' })}
            {...commonProps}
            isMenu
            showFlashyToggle={true}
            showEditOnMenu={true}
            showEditButton={true}
            showReviewOnMenu={true}
            titleNotifier={task.customerAssignments.length}
            onEdit={handleEdit}
            startAdornmentIcon={<StartAdornmentIcon icon={AssignmentIndOutlinedIcon} />}
            onDoubleClick={handleEdit}
            editDialog={isEditOpen && (<TaskAssignmentEditDialog open onClose={handleEditClose} taskId={task.id} />)}
          >
            <TaskAssignmentReadOnly task={task} />
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
            />
          )}
        >
          <FilesReadOnly task={task} style={style} />
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
            titleNotifier={<FeedbackTitle task={task}/>}
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
          <TaskAuditViewersTable />
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
          <TaskAuditCommitsTable />
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
          <TaskAuditQueuesTable />
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
          <TaskAuditProcessesTable />
        </TaskCard>
      );
    case 'audit_flow':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.audit.flow', defaultMessage: 'Audit: Flow' })}
          {...commonProps}
          showFlashyToggle={false}
          showEditOnMenu={false}
          showEditButton={false}
          showReviewOnMenu={false}
          startAdornmentIcon={<StartAdornmentIcon icon={AccountTreeOutlinedIcon} />}>
          <TaskAuditFlow />
        </TaskCard>
      );
    case 'audit_queue_messages':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.audit.queueMessages', defaultMessage: 'Audit: Queue messages' })}
          {...commonProps}
          showFlashyToggle={false}
          showEditOnMenu={false}
          showEditButton={false}
          showReviewOnMenu={false}
          startAdornmentIcon={<StartAdornmentIcon icon={AccountTreeOutlinedIcon} />}>
          <TaskAuditQueueMessagesTable />
        </TaskCard>
      );
    case 'audit_queue_bindings':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.audit.queueBindings', defaultMessage: 'Audit: Queue bindings' })}
          {...commonProps}
          showFlashyToggle={false}
          showEditOnMenu={false}
          showEditButton={false}
          showReviewOnMenu={false}
          startAdornmentIcon={<StartAdornmentIcon icon={AccountTreeOutlinedIcon} />}>
          <TaskAuditQueueBindingsTable />
        </TaskCard>
      );
    case 'audit_queue_deliveries':
      return (
        <TaskCard title={intl.formatMessage({ id: 'taskcard.title.audit.queueDeliveries', defaultMessage: 'Audit: Queue deliveries' })}
          {...commonProps}
          showFlashyToggle={false}
          showEditOnMenu={false}
          showEditButton={false}
          showReviewOnMenu={false}
          startAdornmentIcon={<StartAdornmentIcon icon={AccountTreeOutlinedIcon} />}>
          <TaskAuditQueueDeliveriesTable />
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



const FeedbackTitle: React.FC<{ task: TaskApi.Task }> = ({ task }) => {

  const intl = useIntl();
  const { getOneFeedback } = useFeedback();
  const [feedback, setFeedback] = React.useState<FeedbackApi.Feedback>();

  
  React.useEffect(() => {
    getOneFeedback(task.taskRef!)
      .then((resp) => {
        setFeedback(resp);
      });
  }, [task.taskRef]);


  return feedback ? intl.formatMessage({ id: 'taskcard.title.customerFeedback.published', defaultMessage: 'Published' }) : undefined
}