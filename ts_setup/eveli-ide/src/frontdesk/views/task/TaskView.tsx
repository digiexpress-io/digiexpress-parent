import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { LinearProgress, Container } from '@mui/material';

import { TaskCreate } from './TaskCreate';
import { GroupMember } from '../../types/GroupMember';
import { Comment } from '../../types/task/Comment';

import { ComponentResolver } from '../../context/ComponentResolver';
import { UserGroup } from '../../types/UserGroup';
import { Task } from '../../types/task/Task';
import { TableHeader } from '../../components/TableHeader';
import { useIam } from '@/burger';
import { useFetch } from '@dxs-ts/eveli-fetch';

type OwnProps = {
  taskId?: string
  taskUpdateCallback?: ()=>void
  groups: UserGroup[]
  getUsers: (groupName:string[])=>Promise<GroupMember[]>
  componentResolver?: ComponentResolver
  externalThreads?: boolean
  userSelectionFree?: boolean
}

export const TaskView: React.FC<OwnProps> = (props) => {
  const navigate = useNavigate();
  const [supressConfirmation, setSupressConfirmation] = useState<boolean>();
  const [taskData, setTaskData] = useState<Task|null>(null);
  const [commentData, setCommentData] = useState<Comment[]>([]);
  const { getTaskComments } = useFetch('worker/rest/api/tasks/$taskId/comments.GET', {});
  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});
  const { updateTask } = useFetch('worker/rest/api/tasks/$taskId.PUT', {});
  const { createTask } = useFetch('worker/rest/api/tasks.POST', {});

  const { user } = useIam();

  const navigateBack = ()=> {
    navigate({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/tasks'
    });
  }

  const cancel= () => {
    setTaskData(null);
    navigateBack();
  }
  const loadCommentData = () => {
    if (taskData) {
      getTaskComments(taskData).then(data => setCommentData(data));
    }
    else {
      setCommentData([]);
    }
  }

  function saveTask(task: Task) {
    if (task.id) {
      return updateTask(task);
    } 
    return createTask(task)
  }
  

  const accept = (task:Task) => {
    saveTask(task)
      .then(result => {
        setSupressConfirmation(true);
        return result;
      })
      .then(_result => {
        !!props.taskUpdateCallback && props.taskUpdateCallback();
        setTaskData(null);
        // navigateBack(); this wont work because of react router prompt unmound
      });
  }

  useEffect(()=>{
    if (props.taskId) {
      getTask(props.taskId)
      .then(task => {
        setTaskData(task);
      });
    }
    else {
      let task = {};
      setTaskData(task);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [props.taskId]);

  useEffect(()=>{
    loadCommentData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [taskData]);

  useEffect(() => {
    // react router hack, because you can't make prompt work with unsafe unmount by returning LinearProgress!!!
    if (supressConfirmation === true) {
      setSupressConfirmation(undefined)
      navigateBack();
    }
  }, [supressConfirmation]);

  if (!taskData && supressConfirmation === undefined) {
    return (<LinearProgress/>);
  }

  return (
    <Container maxWidth='lg'>
      <TableHeader id='taskDialog.task'> {taskData?.taskRef || ''}</TableHeader>

      <TaskCreate
        id='taskCreate'
        editTask={taskData ?? {}}
        cancel={cancel}
        handleSubmit={accept}
        groups={props.groups}
        getUsers={props.getUsers}
        componentResolver={props.componentResolver}
        externalThreads={props.externalThreads}
        comments={commentData}
        reloadComments={loadCommentData}
        userSelectionFree={props.userSelectionFree}
        currentUser={user}
        supressConfirmation={supressConfirmation}
      />
    </Container>
    
  );
}
