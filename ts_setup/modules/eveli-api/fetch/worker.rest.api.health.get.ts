import { createFileFetch } from '@dxs-ts/envir-fetch';



export type UserActivityType = 'ACCESS' | 'CHANGE';

export interface EveliHealthUserActivity {
  id: string;
  createdAt: string;
  targetId: string;
  targetIdType: string;
  taskRef: string;
  type: UserActivityType;
  userFor: string;
  userName: string;
}
export const Hook = createFileFetch('worker/rest/api/health.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { url } = params;
  ///.task-activity
  return {

    findAllTaskActivity: async (): Promise<EveliHealthUserActivity[]> => {
      return params.fetch(`${url({})}/task-activity`)
        .then(response => response.json());
    },
    findAllUserActivity: async (): Promise<any[]> => {
      return params.fetch(`${url({})}/user-activity`)
        .then(response => response.json());
    }

  }
}