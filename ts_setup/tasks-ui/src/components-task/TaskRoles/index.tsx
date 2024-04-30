import React from 'react';

import { TaskDescriptor, AssignTaskRoles } from 'descriptor-task';

import { usePopover } from '../TablePopover';
import { SelectTaskRoles } from './SelectTaskRoles';
import { DisplayTaskRoles } from './DisplayTaskRoles';





const TaskRoles: React.FC<{
  task: TaskDescriptor,
  onChange: (command: AssignTaskRoles) => Promise<void>,
  fullnames?: boolean
}> = ({ task, onChange, fullnames }) => {
  const Popover = usePopover();

  return (
    <>
      <DisplayTaskRoles onClick={Popover.onClick} roles={task.roles} fullnames={fullnames}/>
      {Popover.open && <SelectTaskRoles anchorEl={Popover.anchorEl} onChange={onChange} onClose={Popover.onClose} task={task} />}
    </>
  );
}

export default TaskRoles;