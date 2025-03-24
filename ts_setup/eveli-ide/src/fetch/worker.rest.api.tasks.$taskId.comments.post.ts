import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { useIam } from '@/api-iam';
import { TaskApi } from '@/api-task';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/comments.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;
  const { user } = useIam();

  return {
    saveComment: async (commentText:string, replyToId:number|undefined, task: TaskApi.Task, isExternalThread:boolean|undefined): Promise<TaskApi.Comment> => {
      const savingComment = {
        commentText: commentText,
        replyToId: replyToId,
        taskId: task.id,
        external: isExternalThread,
        userName: user.name,
        source: TaskApi.CommentSource.FRONTDESK
      };
      return params.fetch(url({taskId: task.id!}), { method, body: JSON.stringify(savingComment) })
        .then(response => {
          if (response.ok) return response.json();
          throw new Error("Comment save error:" + response.status);
        })
        .then((comment: TaskApi.Comment) => {
          return comment;
        });
    }

  }
}