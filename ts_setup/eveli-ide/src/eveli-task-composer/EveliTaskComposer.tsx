import React from 'react';
import { useNavigate } from '@tanstack/react-router';
import { LinearProgress, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '../api-task';


import { TaskFormState } from './TaskFormState';
import { PageLeavingConfirmation } from './PageLeaveConfirmation';
import { EveliTaskHeader } from './EveliTaskHeader';
import { EveliTaskBody } from './EveliTaskBody';
import { EveliTaskSubHeader } from './EveliTaskSubHeader';
import { EveliTaskFooter } from './EveliTaskFooter';
import { EveliTaskBodyEmpty } from './EveliTaskBodyEmpty';
import { EveliTaskFeatureProvider } from '@/eveli-task-feature';


export type EveliTaskComposerProps = {
  taskId?: string
}

export const EveliTaskComposer: React.FC<EveliTaskComposerProps> = (props) => {
  const [task, setTask] = React.useState<TaskApi.Task | null>();

  const navigate = useNavigate();
  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});
  const { updateTask } = useFetch('worker/rest/api/tasks/$taskId.PUT', {});
  const { createTask } = useFetch('worker/rest/api/tasks.POST', {});


  React.useEffect(() => {
    if (props.taskId && task === undefined) {
      getTask(props.taskId).then(setTask);
    } else if(!props.taskId) {
      setTask(null)
    }
  }, [props.taskId, task]);


  if (task === undefined) {
    return (<LinearProgress/>);
  }


  async function handleSaveTask(task: Partial<TaskApi.Task>) {
    await (task.id ? updateTask(task) : createTask(task as any));
    navigate({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/tasks'
    });
    return;
  }

  async function handleReload() {
    if(props.taskId){
      getTask(props.taskId).then(setTask); 
    }
  }
  const status: TaskApi.TaskStatus = task?.status ?? TaskApi.TaskStatus.NEW;
  const keywords: string[] = (task?.keyWords ?? []).flatMap(element => element.split(','));
  const readOnly: boolean = (status === TaskApi.TaskStatus.COMPLETED || status === TaskApi.TaskStatus.REJECTED || status === TaskApi.TaskStatus.TRANSFERRED);

  return (<>
    <Typography variant='h1'>
      <FormattedMessage id='taskDialog.task' />{' '}
      {task?.taskRef || ''}
    </Typography>

    <EveliTaskFeatureProvider options={task}>
      <TaskFormState task={task} onSubmit={handleSaveTask}>
        {(form) => (
          <>
            <PageLeavingConfirmation navigationConfirmationRequired={() => form.dirty && !form.isSubmitting} />
            <EveliTaskHeader taskId={task?.id} questionnaireId={task?.questionnaireId} form={form} createdAt={task?.created} readOnly={readOnly} keywords={keywords} />
            {task ? <EveliTaskBody task={task} readOnly={readOnly} onReload={handleReload} /> : <EveliTaskBodyEmpty />}
            <EveliTaskSubHeader form={form} readOnly={readOnly} />
            <EveliTaskFooter task={task} form={form} readOnly={readOnly} />
          </>
        )
        }
      </TaskFormState>
    </EveliTaskFeatureProvider>
  </>

  );
}
