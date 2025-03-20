import { QueryResult } from '@material-table/core'
import { Column, Query } from '@material-table/core';

import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { createQueryString } from '../frontdesk/util/tableQuery';
import { ProcExecutionApi } from '@/burger';



export const Hook = createFileFetch('worker/rest/api/processes.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;

  return {
    loadProcesses: async (query: Query<ProcExecutionApi.ProcessExecution>,  columns: Array<Column<ProcExecutionApi.ProcessExecution>>): Promise<QueryResult<ProcExecutionApi.ProcessExecution>> => {
      const queryString = createQueryString(query, columns);
      return params.fetch(url({}) + `?${queryString}`, {
        headers: {
          'Accept': 'application/json'
        },
      })
      .then(response => response.json())
      .then(json => {
        return {
          data: json.content, // array of data
          page: json.pageable.pageNumber, // current page we are on, starts with 0 = first page
          totalCount: json.numberOfElements // total entries on all the pages combined
        }
      })
    }
  }
}