import React from 'react';
import { Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { Outlet, createFileRoute, useNavigate } from '@tanstack/react-router'


import { useEveliPermissions, EveliDatePicker as DateTimePicker, EveliDateTimeFormatter as DateTimeFormatter } from "@dxs-ts/eveli-primitives";
import { useFeedbackBackend, useIam, EveliTenantFeatureEnabled } from '@dxs-ts/eveli-api';
import { FeedbackProvider } from '@dxs-ts/task-feedback';
import { TaskApi, TaskBackendProvider, TaskBackendProviderProps } from '@dxs-ts/task-api';
import { TasksTableProvider } from '@dxs-ts/task-composer-v1';
import { useFetch } from '@dxs-ts/envir-fetch';

import { DialobReview as RealDialobReview } from '../dialob-review';


export const Route = createFileRoute('/secured/$locale/worker/tasks')({
  component: Component,
})

function Component() {
  const { groups } = useFetch('$org/groupsList.GET', {});
  const feedbackBackend = useFeedbackBackend();
  const navigate = useTaskNavigate();
  const permissions = useTaskPermissions();
  const persistence = useTaskPersistence();
  const { user } = useIam();


  const currentUser = React.useMemo(() => ({
    name: user.name || "",
    email: user.email || ""
  }), [user.name, user?.email]);

  return (
      <TaskBackendProvider 
        navigate={navigate} 
        permissions={permissions} 
        persistence={persistence} 
        currentUser={currentUser} 
        roles={groups}
        slots={{ 
          DialobReview, 
          DateTimeFormatter, 
          DateTimePicker
        }}
      >
        <FeedbackProvider backend={feedbackBackend}>
          <TasksTableProvider>
            <Outlet />
          </TasksTableProvider>
        </FeedbackProvider>
      </TaskBackendProvider>)
}


function useTaskNavigate(): TaskBackendProviderProps['navigate'] {
  const navigate = useNavigate();
  return {
    findAllTasks: () => navigate({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/tasks'
    }),
    createOneTask: () => navigate({
      from: '/secured/$locale/worker/tasks',
      to: '/secured/$locale/worker/tasks/create',
    }),
    openOneTask: (taskId: string) => navigate({
      from: '/secured/$locale/worker',
      to:'/secured/$locale/worker/tasks/$taskId',
      params: { taskId }
    }),
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
  const { updateTask } = useFetch('worker/rest/api/tasks/$taskId.PUT', {});
  const { createTask } = useFetch('worker/rest/api/tasks.POST', {});
  const { saveComment } = useFetch('worker/rest/api/tasks/$taskId/comments.POST', {});
  
  const { downloadAttachmentLink } = useFetch('worker/rest/api/tasks/$taskId/files/$filename.GET', {});
  const { addAttachment } = useFetch('worker/rest/api/tasks/$taskId/files.POST', {});
  const { deleteAttachment } = useFetch('worker/rest/api/tasks/$taskId/files/$filename.DELETE', {});
  
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
    paginateTasks: paginateTasks,
    findAllAttachments: loadAttachments,
    findAllUsers: getUsers,
    createOnTaskTransfer: transferTask,
    findAllTasks: findAll,
    getOneTask: getTask,
    modifyOneTask: updateTask,
    deleteOneTask: deleteTask,
    createOneTask: createTask,
    createOneComment: saveComment
  }

  return unit;
}




const DialobReview: React.FC<{task: { id: string, questionnaireId?: string | undefined }}> = ({ task }) => {
  const [open, setOpen] = React.useState(false);
  if(!task.questionnaireId) {
    return (<></>);
  }

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
          onClick={() => setOpen(true)} variant='contained'><FormattedMessage id='task.form.review' /></Button>
      </EveliTenantFeatureEnabled>

      <EveliTenantFeatureEnabled id='FORM_REVIEW_NORMAL'>
        <Button sx={{ padding: '15px', marginTop: '15px', width: '100%'}} onClick={() => setOpen(true)} variant='contained'>
          <FormattedMessage id='task.form.review' />
        </Button>
      </EveliTenantFeatureEnabled>

      {open && <RealDialobReview taskId={task.id} questionnaireId={task.questionnaireId!} onClose={() => setOpen(false)} />}
    </>
  )
}