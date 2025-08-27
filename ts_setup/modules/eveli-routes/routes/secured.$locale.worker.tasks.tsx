import { Outlet, createFileRoute, useNavigate } from '@tanstack/react-router'
import { EveliPermissions, useEveliPermissions } from "@dxs-ts/eveli-primitives";
import { useFeedbackBackend } from '@dxs-ts/eveli-api';
import { FeedbackProvider } from '@dxs-ts/task-feedback';
import { TaskApi, TaskBackendProvider, TaskBackendProviderProps } from '@dxs-ts/task-api';

import { EveliTaskTableProvider } from '../eveli-tasks';
import { useFetch } from '@dxs-ts/envir-fetch';


export const Route = createFileRoute('/secured/$locale/worker/tasks')({
  component: Component,
})

function Component() {
  const feedbackBackend = useFeedbackBackend();
  const navigate = useTaskNavigate();
  const permissions = useTaskPermissions();
  const persistence = useTaskPersistence();

  return (
    <EveliTaskTableProvider>
      <TaskBackendProvider navigate={navigate} permissions={permissions} persistence={persistence}>
        <FeedbackProvider backend={feedbackBackend}>
          <Outlet />
        </FeedbackProvider>
      </TaskBackendProvider>
    </EveliTaskTableProvider>)
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
  const { groups } = useFetch('$org/groupsList.GET', {});
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

    findAllRoles: async function (): Promise<TaskApi.Role[]> {
      return groups;
    },
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