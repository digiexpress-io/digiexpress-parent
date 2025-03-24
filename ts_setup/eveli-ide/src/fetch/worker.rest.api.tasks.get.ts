import React from 'react';

import { createFileFetch } from '@dxs-ts/eveli-fetch';
import { QueryResult } from '@material-table/core'
import { Column, Query } from '@material-table/core';

import { TaskApi } from '@/api-task';

import { createMuiTableQueryString } from '@/api-mui-table';
import { EveliTaskTableContext } from '@/eveli-tasks';


export const Hook = createFileFetch('worker/rest/api/tasks.GET')({
  hook
})

function hook(props: {}) {
  const params = Hook.useParams();
  const { path, contextPath, method, url } = params;
  const tableContext = React.useContext(EveliTaskTableContext);


  return {
    getTasks: async (page=0, size=20): Promise<QueryResult<TaskApi.Task>> => {
      return params.fetch(url({}) + `?page=${page}&size=${size}`)
        .then(response => response.json())
        .then(json => {
          return {
            data: json._embedded?.tasks || [],
            page: json.page.number,
            totalCount: json.page.totalElements
          };
        });
    },

    loadTasks: async (query:Query<TaskApi.Task>, columns:Column<any>[]) => {
      // store paging info to allow restoring of page on navigation back
      let page = query.page;
      let pageSize = query.pageSize;
      const currentPaging = tableContext.paging;
      if (page !== currentPaging?.page || pageSize !== currentPaging?.pageSize) {
        tableContext.setPaging({page, pageSize});
      }
  
      let visibleColumns: any = [];
      const hiddenColumns = columns.map((column: any) => {
        if(column.hidden){
          return column.field
        }else{
          visibleColumns.push(column.field)
          return undefined;
        }
      })
  
      let queryString = createMuiTableQueryString(
        {...query, 
          filters: query.filters.filter((item: any) => !hiddenColumns.includes(item.column.field)), 
          orderByCollection: query.orderByCollection.reduce((accumulator: any[], item: any) => {
            if (item.sortOrder > 0) {
              if (!hiddenColumns.includes(columns[item.orderBy].field)) {
                accumulator.push({
                  ...item,
                  orderBy: visibleColumns.findIndex((visibleColumn: any) => visibleColumn === columns[item.orderBy].field)
                });
              }
            }
            return accumulator;
          }, [])
        }, 
        columns.filter((column: any) => !column.hidden)
      );
  
      return params.fetch(url({}) + `?${queryString}`)
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