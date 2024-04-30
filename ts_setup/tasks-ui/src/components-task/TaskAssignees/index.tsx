import React from 'react';

import { PrincipalId } from 'descriptor-access-mgmt';


import { usePopover } from '../TablePopover';
import { DisplayTaskAssignees } from './DisplayTaskAssignees';
import { SelectTaskAssignees } from './SelectTaskAssignees';


export interface TaskAssigneesProps {
  onChange: (assigneeIds: PrincipalId[]) => Promise<void>;
  task: { assignees: PrincipalId[] };
  fullnames?: boolean; 
  disabled?: boolean;
}

export const TaskAssignees: React.FC<TaskAssigneesProps> = ({ task, onChange, fullnames, disabled }) => {
  const { anchorEl, onClick, onClose, open } = usePopover();

  return (
    <>
      <DisplayTaskAssignees assigneeIds={task.assignees} onClick={onClick} fullnames={fullnames} disabled={disabled} />
      {open && <SelectTaskAssignees anchorEl={anchorEl} onChange={onChange} onClose={onClose} assigneeIds={task.assignees} />}
    </>
  );
}

export default TaskAssignees;