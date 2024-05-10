import React from 'react';
import { ImmutableCustomerStore } from 'descriptor-customer';
import { useBackend } from 'descriptor-backend';
import { ImmutableTaskDescriptor, TaskDescriptor } from 'descriptor-task';
import { useAm } from 'descriptor-access-mgmt';
import { Button } from '@mui/material';
import { useToggle } from 'components-generic';
import { TaskEditDialog } from 'components-task';


export const CustomerTask: React.FC<{task: TaskDescriptor}> = ({task}) => {
  const editTask = useToggle();
  return (
    <>
    <TaskEditDialog open={editTask.open} onClose={editTask.handleEnd} task={task} />
    <Button variant='text' onClick={editTask.handleStart}>
      {task.title}
    </Button>
    </>
  );
}


export const CustomerTasks: React.FC<{customerId: string, children: (task: TaskDescriptor) => React.ReactNode}> = ({customerId, children}) => {
  const [loading, setLoading] = React.useState(false);
  const [tasks, setTasks] = React.useState<TaskDescriptor[]>();
  const backend = useBackend();
  const am = useAm();

  React.useEffect(() => {
      setLoading(true);
      new ImmutableCustomerStore(backend.store).findCustomerTasks(customerId).then(newRecords => {
        const today = new Date();
        setTasks(newRecords.map(t => new ImmutableTaskDescriptor(t, am.profile, today)));
        setLoading(false);
      });
    
  }, [customerId]);

  if(loading) {
    return (<></>);
  }

  return (<>{tasks?.map(task => children(task))} </>);
}