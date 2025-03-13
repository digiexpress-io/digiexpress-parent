import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { Task } from '../frontdesk/types/task/Task';
import { Comment, CommentSource } from '../frontdesk/types/task/Comment';
import { useIam } from '@/burger';


export const Hook = createFileFetch('worker/rest/api/tasks/$taskId/comments.POST')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;
  const { user } = useIam();

  return {
    saveComment: async (commentText:string, replyToId:number|undefined, task:Task, isExternalThread:boolean|undefined): Promise<Comment> => {
      const savingComment = {
        commentText: commentText,
        replyToId: replyToId,
        taskId: task.id,
        external: isExternalThread,
        userName: user.name,
        source: CommentSource.FRONTDESK
      };
      return params.fetch(url({taskId: task.id!}), { method, body: JSON.stringify(savingComment) })
        .then(response => {
          if (response.ok) return response.json();
          throw new Error("Comment save error:" + response.status);
        })
        .then((comment: Comment) => {
          return comment;
        });
    }

  }
}