import React, { useEffect, useState } from 'react';

import { Container } from '@mui/material';
import { useNavigate } from "@tanstack/react-router";
import { useFetch } from '@dxs-ts/eveli-fetch';

import { useIam, useConfig } from '@/burger';
import { TasksTable } from './TasksTable';


export const TasksView: React.FC = () => {
  const navigate = useNavigate();
  const { taskDeleteGroups } = useConfig();
  const { user } = useIam();
  const [newTasks, setNewTasks] = useState<string[]>([]);
  const { groups } = useFetch('$org/groupsList.GET', {});
  const { loadTasks } = useFetch('worker/rest/api/tasks.GET', {});
  const { loadNewTasks } = useFetch('worker/rest/api/tasks/unread.GET', {});

  const taskOpenCallback = (taskId: string | undefined) => {
    

    if(taskId) {
      navigate({
        from: '/secured/$locale/worker/tasks',
        params: { taskId },
        to: '/secured/$locale/worker/tasks/$taskId',
      });
    } else {
      navigate({
        from: '/secured/$locale/worker/tasks',
        to: '/secured/$locale/worker/tasks/create',
      });
    }
  }

  const taskDeletableCallback = () => {
    if (taskDeleteGroups && taskDeleteGroups.length > 0) {
      if (user.hasRole(...taskDeleteGroups)) {
        return true;
      }
      return false;
    }
    return true;
  }



  useEffect(() => {
    loadNewTasks().then(newTasks => setNewTasks(newTasks));
  },[])


  return (
    <Container maxWidth='xl'>
      <TasksTable loadTasks={loadTasks} groups={groups} taskOpenHandler={taskOpenCallback} 
      taskDeletableHandler={taskDeletableCallback}
      newTasks={newTasks}/>
    </Container>
  )
};
