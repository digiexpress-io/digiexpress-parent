import { createFileFetch } from '@dxs-ts/envir-fetch';
import { QueryResult } from '@material-table/core'
import { TaskApi } from '@dxs-ts/task-api';


export const Hook = createFileFetch('worker/rest/api/tasks.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;


  return {
    dashboard: async (): Promise<TaskApi.TaskDasboard> => {
      return params.fetch(url({}) + `/dashboard`)
        .then(response => response.json());
    },

    findAll: async (): Promise<TaskApi.Task[]> => {
      return params.fetch(url({}) + `/all`)
        .then(response => response.json());
    },

    getTasks: async (page=0, size=20): Promise<QueryResult<TaskApi.Task>> => {
      return params.fetch(url({}) + `?page=${page}&size=${size}`)
        .then(response => response.json())
        .then(json => {
          return {
            data: json.content, // array of data
            page: json.pageable.pageNumber, // current page we are on, starts with 0 = first page
            totalCount: json.totalElements // total entries on all the pages combined
          };
        });
    },

    paginateTasks: async (query: string) => {
      return params.fetch(url({}) + `?${query}`)
      .then(response => response.json())
      .then(json => {
        return {
          data: json.content, // array of data
          page: json.pageable.pageNumber, // current page we are on, starts with 0 = first page
          totalCount: json.totalElements // total entries on all the pages combined
        };
      });
    }
  }
}