import React from 'react';

import { FormattedMessage } from 'react-intl';
import { TaskApi } from '@/api-task';
import { CreateTaskTransfer } from './CreateTaskTransfer';

export interface EveliTaskTransferProps {
  task: TaskApi.Task;
  onTransferComplete: () => void;
}

export const EveliTaskTransfer: React.FC<EveliTaskTransferProps> = (props) => {

  if(props.task.transferredId) {
    return <>
      <FormattedMessage id='task.transfer.published.done'/>: {props.task.transferredId}
    </>
  }

  return (<CreateTaskTransfer task={props.task} onTransferComplete={props.onTransferComplete}/>);
}