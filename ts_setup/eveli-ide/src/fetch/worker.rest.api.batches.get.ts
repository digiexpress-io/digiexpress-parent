import React from 'react';

import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { QueryResult } from '@material-table/core'
import { Column, Query } from '@material-table/core';

import { TaskApi } from '@/api-task';

import { createMuiTableQueryString } from '@/api-mui-table';
import { EveliTaskTableContext } from '@/eveli-tasks';
import { BatchApi } from '@/api-batch';


export const Hook = createFileFetch('worker/rest/api/batches.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;


  return {
    findAll: async (): Promise<BatchApi.Batch[]> => {
      return params.fetch(url({}) )
        .then(response => response.json());
    },
  }
}