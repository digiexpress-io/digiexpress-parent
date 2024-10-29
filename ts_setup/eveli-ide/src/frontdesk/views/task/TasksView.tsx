import React, { useContext, useEffect, useState } from 'react';
import { Container } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { TasksTable } from './TasksTable';
import { Column, Query } from '@material-table/core';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { useConfig } from '../../context/ConfigContext';
import { createQueryString } from '../../util/tableQuery';
import { useUserInfo } from '../../context/UserContext';
import { TableStateContext } from '../../context/TaskSessionContext';
import { Task } from '../../types/task/Task';
import { UserGroup } from '../../types/UserGroup';
import { UserBackendContext } from '../../context/TaskUserContext';


export const TasksView: React.FC = () => {
  const navigate = useNavigate();
  const userContext = useContext(UserBackendContext);
  const [groups, setGroups] = useState<UserGroup[]>([]);
  const session = useContext(SessionRefreshContext);
  const {tasksApiUrl, taskDeleteGroups} = useConfig();
  const userInfo = useUserInfo();
  const [newTasks, setNewTasks] = useState<number[]>([]);
  const tableContext = useContext(TableStateContext);

  const taskOpenCallback = (id:number|undefined) => {
    let taskId = id ? id : '';
    navigate(`/ui/tasks/task/${taskId}`);
  }

  const taskDeletableCallback = (task:Task) => {
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

    return session.cFetch(`${tasksApiUrl}/taskSearch?${queryString}`)
    .then(response => response.json())
    .then(json=>{
      return {
        data: json.content || [],
        page: json.number,//json.page.number || 0,
        totalCount: 1// json.page.totalElements || 0
      };
    });
  }

  const loadNewTasks = () => {
    return session.cFetch(`${tasksApiUrl}/tasksUnread`)
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
