import React from 'react';

import { TaskApi } from '@dxs-ts/task-api';
import { CreateTaskTransfer } from './CreateTaskTransfer';

export interface EveliTaskTransferProps {
  task: TaskApi.Task;
  onTransferComplete: () => void;
}

export const EveliTaskTransfer: React.FC<EveliTaskTransferProps> = (props) => {

  /*
  if(props.task.transferredId) {
    return <>
      <FormattedMessage id='task.transfer.published.done'/>: {props.task.transferredId}
    </>
  }*/

  return (<CreateTaskTransfer task={props.task} onTransferComplete={props.onTransferComplete}/>);
}