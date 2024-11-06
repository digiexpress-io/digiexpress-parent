import React, { useContext, useEffect, useState } from 'react';
import { Container } from '@mui/material';

import { Column, Query } from '@material-table/core';
import { useNavigate } from 'react-router-dom';

import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { useConfig } from '../../context/ConfigContext';
import { useUserInfo } from '../../context/UserContext';
import { TableStateContext } from '../../context/TaskSessionContext';
import { UserBackendContext } from '../../context/TaskUserContext';

import { createQueryString } from '../../util/tableQuery';

import { Task } from '../../types/task/Task';
import { UserGroup } from '../../types/UserGroup';

import { TasksTable } from './TasksTable';


export const TasksView: React.FC = () => {
  const navigate = useNavigate();
  const userContext = useContext(UserBackendContext);
  const [groups, setGroups] = useState<UserGroup[]>([]);
  const session = useContext(SessionRefreshContext);
  const { serviceUrl, taskDeleteGroups } = useConfig();
  const userInfo = useUserInfo();
  const [newTasks, setNewTasks] = useState<number[]>([]);
  const tableContext = useContext(TableStateContext);

  const taskOpenCallback = (id:number|undefined) => {
    let taskId = id ? id : '';
    navigate(`/ui/tasks/task/${taskId}`);
  }

  const taskDeletableCallback = () => {
    if (taskDeleteGroups && taskDeleteGroups.length > 0) {
      if (userInfo.hasRole(...taskDeleteGroups)) {
        return true;
      }
      return false;
    }
    return true;
  }

  const loadTasks = (query:Query<Task>, columns:Column<any>[]) => {
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

    let queryString = createQueryString(
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

    return session.cFetch(`${serviceUrl}rest/api/worker/tasks?${queryString}`)
    .then(response => response.json())
    .then(json=>{
      return {
        data: json.content, // array of data
        page: json.pageable.pageNumber, // current page we are on, starts with 0 = first page
        totalCount: json.numberOfElements // total entries on all the pages combined
      };
    });
  }

  const loadNewTasks = () => {
    return session.cFetch(`${serviceUrl}rest/api/worker/tasks/unread`)
    .then(response => response.json())
    .then(json=>{
      return json;
    });
  }

  useEffect(() => {
    userContext.getGroups()
      .then(groups => setGroups(groups));
    loadNewTasks()
    .then(newTasks=>setNewTasks(newTasks));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  },[userContext])


  return (
    <Container maxWidth='xl'>
      <TasksTable loadTasks={loadTasks} groups={groups} taskOpenHandler={taskOpenCallback} 
      taskDeletableHandler={taskDeletableCallback}
      newTasks={newTasks}/>
    </Container>
  )
};
