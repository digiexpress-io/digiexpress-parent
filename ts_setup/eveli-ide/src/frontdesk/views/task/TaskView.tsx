import React, { useContext, useEffect, useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { useNavigate } from "react-router-dom";
import { LinearProgress, Container, Typography } from '@mui/material';

import {TaskForm} from './TaskForm';
import { GroupMember } from '../../types/GroupMember';
import { Comment } from '../../types/task/Comment';
import { TaskBackendContext } from '../../context/TaskApiConfigContext';
import { ComponentResolver } from '../../context/ComponentResolver';
import { UserGroup } from '../../types/UserGroup';
import { Task } from '../../types/task/Task';
import { useUserInfo } from '../../context/UserContext';

type OwnProps = {
  taskId?: number
  taskUpdateCallback?: ()=>void
  groups: UserGroup[]
  getUsers: (groupName:string[])=>Promise<GroupMember[]>
  componentResolver?: ComponentResolver
  externalThreads?: boolean
  userSelectionFree?: boolean
}

type Props = OwnProps;

export const TaskView:React.FC<Props> = (props) =>{
  const [taskData, setTaskData] = useState<Task|null>(null);
  const [commentData, setCommentData] = useState<Comment[]>([]);
  const taskContext = useContext(TaskBackendContext);
  let navigate = useNavigate();
  const userInfo = useUserInfo();

  const navigateBack = ()=> {
    navigate('/ui/tasks');
  }

  const cancel= () => {
    setTaskData(null);
    navigateBack();
  }
  const loadCommentData = () => {
    if (taskData) {
      taskContext.getTaskComments(taskData)
      .then(data => setCommentData(data));
    }
    else {
      setCommentData([]);
    }
  }

  const accept = (task:Task) => {
    taskContext.saveTask(task)
    .then(result=>{!!props.taskUpdateCallback && props.taskUpdateCallback();return result;})
    .then(result=>{setTaskData(null); return result;})
    .then(result=> navigateBack());
  }

  useEffect(()=>{
    if (props.taskId) {
      taskContext.getTask(props.taskId)
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

  if (!taskData) {
    return (<LinearProgress/>);
  }
  return (
    <Container maxWidth='lg'>
      <Typography variant='h2' m={2}>
        <FormattedMessage id='taskDialog.task' /> {taskData.taskRef || ''}
      </Typography>
      <TaskForm
        id='taskForm'
        editTask={taskData}
        cancel={cancel}
        handleSubmit={accept}
        groups={props.groups}
        getUsers={props.getUsers}
        componentResolver={props.componentResolver}
        externalThreads={props.externalThreads}
        comments={commentData}
        reloadComments={loadCommentData}
        userSelectionFree={props.userSelectionFree}
        currentUser={userInfo?.user}
      />
    </Container>
    
  );
}
