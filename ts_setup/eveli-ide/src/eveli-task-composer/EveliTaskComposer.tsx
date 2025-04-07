import React from 'react';
import { useNavigate } from '@tanstack/react-router';
import { LinearProgress, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { useFetch } from '@dxs-ts/eveli-fetch';
import { useIam } from '../api-iam';
import { TaskApi } from '../api-task';


import { TaskCreate } from './TaskCreate';
import { TasksComponentResolver } from './TaskComponentResolver';


export type EveliTaskComposerProps = {
  taskId?: string
}

export const EveliTaskComposer: React.FC<EveliTaskComposerProps> = (props) => {
  const navigate = useNavigate();
  const { user } = useIam();
  const [supressConfirmation, setSupressConfirmation] = React.useState<boolean>();
  const [taskData, setTaskData] = React.useState<TaskApi.Task|null>(null);
  const [commentData, setCommentData] = React.useState<TaskApi.Comment[]>([]);

  const { groups } = useFetch('$org/groupsList.GET', {});
  const { getUsers } = useFetch('$org/groupMembership.GET', {});
  const { pdfTaskLinkCallback } = useFetch('worker/rest/api/pdf.GET', {});
  const { getTaskComments } = useFetch('worker/rest/api/tasks/$taskId/comments.GET', {});
  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});
  const { updateTask } = useFetch('worker/rest/api/tasks/$taskId.PUT', {});
  const { createTask } = useFetch('worker/rest/api/tasks.POST', {});

  
  const handleNavigateBack = ()=> {
    navigate({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/tasks'
    });
  }

  function handleCancel() {
    setTaskData(null);
    handleNavigateBack();
  }

  const loadCommentData = () => {
    if (taskData) {
      getTaskComments(taskData).then(data => setCommentData(data));
    } else {
      setCommentData([]);
    }
  }  

  const accept = async (task: TaskApi.Task) => {
    const saved = await (task.id ? updateTask(task) : createTask(task));
    setSupressConfirmation(true);
    setTaskData(null);
    return;
  }

  React.useEffect(()=>{
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

  React.useEffect(()=>{
    loadCommentData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [taskData]);

  React.useEffect(() => {
    // react router hack, because you can't make prompt work with unsafe unmount by returning LinearProgress!!!
    if (supressConfirmation === true) {
      setSupressConfirmation(undefined)
      handleNavigateBack();
    }
  }, [supressConfirmation]);

  if (!taskData && supressConfirmation === undefined) {
    return (<LinearProgress/>);
  }

  return (<>
      <Typography variant='h1'>
        <FormattedMessage id='taskDialog.task'/>
        {taskData?.taskRef || ''}
      </Typography>

      <TaskCreate
        id='taskCreate'
        editTask={taskData ?? {}}
        cancel={handleCancel}
        handleSubmit={accept}
        groups={groups}
        getUsers={getUsers}
        componentResolver={new TasksComponentResolver(() => {}, pdfTaskLinkCallback)}
        externalThreads={true}
        comments={commentData}
        reloadComments={loadCommentData}
        userSelectionFree={true}
        currentUser={user}
        supressConfirmation={supressConfirmation}
      />
    </>
    
  );
}
