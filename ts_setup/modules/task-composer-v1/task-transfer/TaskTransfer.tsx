import React from 'react';

import { TaskApi } from '@dxs-ts/task-api';
import { CreateTaskTransfer } from './CreateTaskTransfer';

export interface TaskTransferProps {
  task: TaskApi.Task;
  onTransferComplete: () => void;
}

export const TaskTransfer: React.FC<TaskTransferProps> = (props) => {

  /*
  if(props.task.transferredId) {
    return <>
      <FormattedMessage id='task.transfer.published.done'/>: {props.task.transferredId}
    </>
  }*/

  return (<CreateTaskTransfer task={props.task} onTransferComplete={props.onTransferComplete}/>);
}