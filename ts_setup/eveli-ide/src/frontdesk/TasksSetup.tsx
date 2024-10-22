import React, { PropsWithChildren } from 'react';
import { useConfig } from './context/ConfigContext';
import { TaskSessionContext } from './context/TaskSessionContext';

export const TasksSetup: React.FC<PropsWithChildren> = ({children}) => {
  const config = useConfig();
    return (
    <TaskSessionContext apiBaseUrl={config.tasksApiUrl || ''}>
      {children}
    </TaskSessionContext>
  );
};
