import React from 'react';
import { TaskApi } from '@dxs-ts/task-api';

export const TaskAdditionalInfo: React.FC<{ task: TaskApi.Task }> = ({ task }) => {

  return (
    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '10ch' }}>
      {task.additionalInfo}
    </div>
  );
}
