import React from 'react';
import { Tooltip } from '@mui/material';

import { useIntl } from 'react-intl';
import { TaskApi } from '@dxs-ts/task-api';

export interface EveliTaskTransferStatusIndicatorProps {
  task: TaskApi.Task;
}

export const EveliTaskTransferStatusIndicator: React.FC<EveliTaskTransferStatusIndicatorProps> = (props) => {

  const intl = useIntl();

  return (
    <Tooltip title={intl.formatMessage({ id: 'task.transfer.isPublished' })}>
      <></>
    </Tooltip>
    );
}