import React from 'react';
import { Box, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { useNavigate } from '@tanstack/react-router'

import { useEveliPermissions } from "@dxs-ts/eveli-primitives";
import { DatePicker as XuiDatePicker, DateTimeFormatter as XuiDateTimeFormatter } from "@dxs-ts/xui-datetime";
import { DateTime } from "luxon";

import { useFeedbackBackend, useIam, EveliTenantFeatureEnabled, useTenantConfigFeatures } from '@dxs-ts/eveli-api';
import { FeedbackProvider } from '@dxs-ts/task-feedback';
import { TaskApi, TaskBackendProvider, TaskBackendProviderProps } from '@dxs-ts/task-api';
import { useFetch } from '@dxs-ts/envir-fetch';

import { DialobReviewBasedOnForm, DialobReview as RealDialobReview } from '../dialob-review';
import { TaskCreate } from '@dxs-ts/task-composer-v2';


export const AnyTaskRoute: React.FC<{children: React.ReactNode}> = ({ children }) => {
  const { unreadTasks } = useFetch('worker/rest/api/tasks/unread.GET', {});
  const { groups } = useFetch('$org/groupsList.GET', {});
  const feedbackBackend = useFeedbackBackend();
  const [open, setOpen] = React.useState(false);
  const navigate = useTaskNavigate({ setTaskCreateOpen: setOpen });
  const permissions = useTaskPermissions();
  const persistence = useTaskPersistence();
  const features = useTaskFeatures();
  const { user } = useIam();

  const currentUser = React.useMemo(() => ({
    name: user.name || "",
    email: user.email || ""
  }), [user.name, user?.email]);

  const SlotDateTimePicker: React.FC<{
    value?: string | Date | null;
    onChange?: (d: Date | null) => void;
    onKeyDown?: React.KeyboardEventHandler;
    readonly?: boolean;
    fullWidth?: boolean;
    size?: "small" | "medium";
    label?: React.ReactNode;
  }> = ({ value, onChange, onKeyDown, readonly, fullWidth = true, size = "small" }) => {

    const normalized: Date | null =
      typeof value === "string"
        ? value
          ? DateTime.fromISO(value).toJSDate()
          : null
        : value ?? null;
  
    return (
      <Box onKeyDown={onKeyDown}>
        <XuiDatePicker
          fullWidth={fullWidth}
          size={size}
          value={normalized}
          onChange={readonly ? () => {} : (d) => onChange?.(d)}
          sx={{ pointerEvents: readonly ? "none" : "auto" }}
        />
      </Box>
    );
  };  

  const SlotDateTimeFormatter: React.FC<{ value: any; variant?: "text" }> = ({
    value,
    variant,
  }) => <XuiDateTimeFormatter value={value} variant={variant} />;


  function handleClose() {
    setOpen(false);
  }

  return (
    <TaskBackendProvider 
      deps={[unreadTasks]}
      navigate={navigate}
      permissions={permissions}
      persistence={persistence}
      currentUser={currentUser}
      roles={groups}
      features={features}
      slots={{
        DateTimeFormatter: SlotDateTimeFormatter,
        DateTimePicker: SlotDateTimePicker,
        DialobReview,
        DialobReviewButton
      }}
      >
        <FeedbackProvider backend={feedbackBackend}>
          {children}
          <TaskCreate open={open} onClose={handleClose}/>
        </FeedbackProvider>
      </TaskBackendProvider>)
}


function useTaskNavigate(props: { setTaskCreateOpen: (open: boolean) => void}): TaskBackendProviderProps['navigate'] {
  const navigate = useNavigate();
  return {
    findAllTasks: () => navigate({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/tasks'
    }),
    createOneTask: () => {
      props.setTaskCreateOpen(true);
    },
    openOneTask: (taskId: string) => navigate({
      from: '/secured/$locale/worker',
      to:'/secured/$locale/worker/tasks/$taskId',
      params: { taskId }
    }),
  }
}

function useTaskFeatures(): TaskBackendProviderProps['features'] {
  const tenant = useTenantConfigFeatures()
  return {
    isAuditTaskEnabled: tenant.isEnabled('SMART_TASK_AUDIT'),
  }
}

function useTaskPermissions(): TaskBackendProviderProps['permissions'] {
  const permissions = useEveliPermissions();
  return {
    isCreateTaskAllowed: permissions.isAccessGranted('CREATE_TASK'),
    isDeleteTaskAllowed: permissions.isAccessGranted('DELETE_TASK'),
    isReopenTaskAllowed: permissions.isAccessGranted('TASK_REOPEN'),
    isRetransferAllowed: permissions.isAccessGranted('TASK_RETRANSFER'),
  }
}

function useTaskPersistence(): TaskBackendProviderProps['persistence'] {

  const { getUsers } = useFetch('$org/groupMembership.GET', {});

  const { unreadTasks, refetch } = useFetch('worker/rest/api/tasks/unread.GET', {});
  const { findAll } = useFetch('worker/rest/api/tasks.GET', {});
  const { deleteTask } = useFetch('worker/rest/api/tasks/$taskId.DELETE', {});

  const { transferTask } = useFetch('worker/rest/api/tasks/$taskId/transfers.PUT', {})
  const { loadAttachments } = useFetch('worker/rest/api/tasks/$taskId/files.GET', {});
  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});
  const { getOneTaskAudit } = useFetch('worker/rest/api/tasks/$taskId/audits.GET', {});

  const { updateTask } = useFetch('worker/rest/api/tasks/$taskId.PUT', {});
  const { createTask } = useFetch('worker/rest/api/tasks.POST', {});
  const { saveComment } = useFetch('worker/rest/api/tasks/$taskId/comments.POST', {});
  
  const { downloadAttachmentLink } = useFetch('worker/rest/api/tasks/$taskId/files/$filename.GET', {});
  const { addAttachment } = useFetch('worker/rest/api/tasks/$taskId/files.POST', {});
  const { deleteAttachment } = useFetch('worker/rest/api/tasks/$taskId/files/$filename.DELETE', {});
  const { pdfTaskCallback } = useFetch('worker/rest/api/pdf.POST', {});
  const { getTaskFormAssignment } = useFetch('worker/rest/api/tasks/$taskId/form-assignments.GET', []);
  const { createManyTaskCustomerAssignments } = useFetch('worker/rest/api/tasks/$taskId/form-assignments.POST', []);

  const { deleteManyCustomerAssignment } = useFetch('worker/rest/api/tasks/$taskId/form-assignments.DELETE', {})

  const unit:  TaskBackendProviderProps['persistence'] = {
    findAllUnreadTasks: async function (): Promise<string[]> {
      return unreadTasks;
    },
    deleteOneAttachment: async function (taskId: string, attachment: TaskApi.Attachment): Promise<unknown> {
      return deleteAttachment(taskId, attachment.name);
    },

    getOneAttachmentLink: async function (taskId: string, attachment: TaskApi.Attachment): Promise<string> {
      return downloadAttachmentLink(taskId, attachment.name);
    },
    createManyAttachments: async function (taskId: string, files: FileList): Promise<unknown> {
      const promises = Array.from(files).map((file, index) => addAttachment(taskId, file));
      await Promise.all(promises);
      return {};
    },
    deleteManyCustomerAssignment,
    getOneTaskAudit: getOneTaskAudit,
    findAllAttachments: loadAttachments,
    findAllUsers: getUsers,
    createOnTaskTransfer: transferTask,
    findAllTasks: findAll,
    getOneTask: async (taskId: string): Promise<TaskApi.Task> => {
      const task = await getTask(taskId);
      if (unreadTasks.includes(task.id)) {
        refetch();
      }
      return task;
    },
    modifyOneTask: updateTask,
    deleteOneTask: deleteTask,
    createOneTask: createTask,
    createOneComment: saveComment,
    getOneTaskPdf: pdfTaskCallback,
    findAllTaskFormAssignments: getTaskFormAssignment,
    createManyTaskCustomerAssignments: createManyTaskCustomerAssignments
  }

  return unit;
}




const DialobReview: React.FC<{task: { id: string, questionnaireId?: string | undefined }; onClose: () => void }> = ({ onClose, task }) => {
  if(!task.questionnaireId) {
    return (<></>);
  }
  return (
    <>
      {/* <RealDialobReview taskId={task.id} questionnaireId={task.questionnaireId!} onClose={onClose} /> */}
      <DialobReviewBasedOnForm taskId={task.questionnaireId ?? task.id} questionnaireId={task.questionnaireId!} onClose={onClose} />
    </>
  )
}

const DialobReviewButton: React.FC<{ onClick: () => void; }> = ({ onClick }) => {
  return (
    <>
      <EveliTenantFeatureEnabled id='FORM_REVIEW_FLASHY'>
        <Button sx={{ padding: '15px', marginTop: '15px', width: '100%',  
            animation: 'pulse 1.5s ease-in-out infinite',
            transition: 'transform 0.3s ease-in-out',
            '@keyframes pulse': {
              '0%': { transform: 'scale(1)', opacity: 1 },
              '50%': { transform: 'scale(1.05)', opacity: 0.8 },
              '100%': { transform: 'scale(1)', opacity: 1 },
          }}} 
          onClick={onClick} variant='contained'><FormattedMessage id='task.form.review' /></Button>
      </EveliTenantFeatureEnabled>

      <EveliTenantFeatureEnabled id='FORM_REVIEW_NORMAL'>
        <Button sx={{ padding: '15px', marginTop: '15px', width: '100%'}} onClick={onClick} variant='contained'>
          <FormattedMessage id='task.form.review' />
        </Button>
      </EveliTenantFeatureEnabled>
    </>
  )
}