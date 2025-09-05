import React from 'react';
import { Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { useNavigate } from '@tanstack/react-router'


import { useEveliPermissions, EveliDatePicker as DateTimePicker, EveliDateTimeFormatter as DateTimeFormatter } from "@dxs-ts/eveli-primitives";
import { useFeedbackBackend, useIam, EveliTenantFeatureEnabled, useTenantConfigFeatures } from '@dxs-ts/eveli-api';
import { FeedbackProvider } from '@dxs-ts/task-feedback';
import { TaskApi, TaskBackendProvider, TaskBackendProviderProps } from '@dxs-ts/task-api';
import { TasksTableProvider } from '@dxs-ts/task-composer-v1';
import { useFetch } from '@dxs-ts/envir-fetch';

import { DialobReviewBasedOnForm, DialobReview as RealDialobReview } from '../dialob-review';
import { TaskCreate } from '@dxs-ts/task-composer-v2';


export const AnyTaskRoute: React.FC<{children: React.ReactNode}> = ({ children }) => {
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



  function handleClose() {
    setOpen(false);
  }

  return (
      <TaskBackendProvider 
        navigate={navigate} 
        permissions={permissions} 
        persistence={persistence} 
        currentUser={currentUser} 
        roles={groups}
        features={features}
        slots={{ 
          DateTimeFormatter, 
          DateTimePicker,
          DialobReview, 
          DialobReviewButton
        }}
      >
        <FeedbackProvider backend={feedbackBackend}>
          <TasksTableProvider>
            {children}
            <TaskCreate open={open} onClose={handleClose}/>
          </TasksTableProvider>
        </FeedbackProvider>
      </TaskBackendProvider>)
}


function useTaskNavigate(props: { setTaskCreateOpen: (open: boolean) => void}): TaskBackendProviderProps['navigate'] {
  const navigate = useNavigate();
  const tenant = useTenantConfigFeatures();
  
  
  return {
    findAllTasks: () => navigate({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/tasks'
    }),
    createOneTask: () => {
      if(tenant.isEnabled('SMART_TASK')) {
        props.setTaskCreateOpen(true);
      } else {
        navigate({
          from: '/secured/$locale/worker/tasks',
          to: '/secured/$locale/worker/tasks/create',
        })
      }
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
  }
}

function useTaskPersistence(): TaskBackendProviderProps['persistence'] {

  const { getUsers } = useFetch('$org/groupMembership.GET', {});

  const { unreadTasks } = useFetch('worker/rest/api/tasks/unread.GET', {});
  const { paginateTasks, findAll } = useFetch('worker/rest/api/tasks.GET', {});
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
  const { pdfTaskLinkCallback } = useFetch('worker/rest/api/pdf.GET', {});

  const unit:  TaskBackendProviderProps['persistence'] = {
    findAllUnreadTasks: async function (): Promise<string[]> {
      return unreadTasks;
    },
    deleteOneAttachment: async function (taskId: string, attachment: TaskApi.Attachment): Promise<unknown> {
      return deleteAttachment(taskId, attachment.name)
    },

    getOneAttachmentLink: async function (taskId: string, attachment: TaskApi.Attachment): Promise<string> {
      return downloadAttachmentLink(taskId, attachment.name)
    },
    createManyAttachments: async function (taskId: string, files: FileList): Promise<unknown> {
      const promises = Array.from(files).map((file, index) => addAttachment(taskId, file))
      await Promise.all(promises);
      return {};
    },
    getOneTaskAudit: getOneTaskAudit,
    paginateTasks: paginateTasks,
    findAllAttachments: loadAttachments,
    findAllUsers: getUsers,
    createOnTaskTransfer: transferTask,
    findAllTasks: findAll,
    getOneTask: getTask,
    modifyOneTask: updateTask,
    deleteOneTask: deleteTask,
    createOneTask: createTask,
    createOneComment: saveComment,
    getOneTaskPdfLink: pdfTaskLinkCallback
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
      <DialobReviewBasedOnForm taskId={task.id} questionnaireId={task.questionnaireId!} onClose={onClose} />
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