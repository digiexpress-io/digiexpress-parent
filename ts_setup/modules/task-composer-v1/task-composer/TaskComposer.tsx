import React from 'react';
import { LinearProgress, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { TaskApi, TaskFeatureProvider, useTaskBackend } from '@dxs-ts/task-api';


import { TaskFormState } from './TaskFormState';
import { PageLeavingConfirmation } from './PageLeaveConfirmation';
import { TaskHeader } from './TaskHeader';
import { TaskBody } from './TaskBody';
import { TaskSubHeader } from './TaskSubHeader';
import { TaskFooter } from './TaskFooter';
import { TaskBodyEmpty } from './TaskBodyEmpty';

import { TaskReopen } from './TaskReopen';


export type TaskComposerProps = {
  taskId?: string
}

export const TaskComposer: React.FC<TaskComposerProps> = (props) => {
  const backend = useTaskBackend();
  
  const [task, setTask] = React.useState<TaskApi.Task | null>();
  
  React.useEffect(() => {
    if (props.taskId && task === undefined) {
      backend.persistence.getOneTask(props.taskId).then(setTask);
    } else if(!props.taskId) {
      setTask(null)
    }
  }, [props.taskId, task]);


  if (task === undefined) {
    return (<LinearProgress/>);
  }



  async function handleSaveTask(task: Partial<TaskApi.Task>) {
    await (task.id ? backend.persistence.modifyOneTask(task as any) : backend.persistence.createOneTask(task as any));
    backend.navigate.findAllTasks();
    return;
  }

  async function handleReload() {
    if(props.taskId){
      backend.persistence.getOneTask(props.taskId).then(setTask); 
    }
  }

  const status: TaskApi.TaskStatus = task?.status ?? TaskApi.TaskStatus.NEW;
  const keywords: string[] = (task?.keyWords ?? []).flatMap(element => element.split(','));
  const readOnly: boolean = (status === TaskApi.TaskStatus.COMPLETED || status === TaskApi.TaskStatus.REJECTED 
    || status === TaskApi.TaskStatus.TRANSFERRED || status === TaskApi.TaskStatus.DELEGATED);

  return (<>
    <Typography variant='h1'>
      <FormattedMessage id='taskDialog.task' />{' '}
      {task?.taskRef || ''}
    </Typography>

    <TaskFeatureProvider options={task}>
      <TaskFormState task={task} onSubmit={handleSaveTask}>
        {(form) => (
          <>
            <PageLeavingConfirmation navigationConfirmationRequired={() => form.dirty && !form.isSubmitting} />
            <TaskHeader taskId={task?.id} questionnaireId={task?.questionnaireId} form={form} createdAt={task?.created} readOnly={readOnly} keywords={keywords} />
            {task ? <TaskBody task={task} readOnly={readOnly} onReload={handleReload} /> : <TaskBodyEmpty />}
            <TaskSubHeader form={form} readOnly={readOnly} slots={{ statusExtra: task && <TaskReopen task={task} onReload={handleReload} /> }} />
            <TaskFooter task={task} form={form} readOnly={readOnly} />
          </>
        )
        }
      </TaskFormState>
    </TaskFeatureProvider>
  </>

  );
}
