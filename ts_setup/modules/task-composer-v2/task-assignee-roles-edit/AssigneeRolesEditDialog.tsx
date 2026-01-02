import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, Grid2, styled, Typography, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';

import { useTaskDashboard } from '../task-dashboard';
import { EditRoles } from './EditRoles';
import { TaskCreateAssignee } from '../task-create/TaskCreateAssignee';


export interface AssigneeRolesEditDialogProps {
  open: boolean;
  onClose: () => void
}


export const AssigneeRolesEditDialog: React.FC<AssigneeRolesEditDialogProps> = ({ open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const backend = useTaskBackend();
  const { task, saveTask, isTaskChanged } = useTaskDashboard();

  const [roles, setRoles] = React.useState<string[]>(task.assignedRoles ?? []);
  const [userList, setUserList] = React.useState<TaskApi.User[]>([]);
  const [assignee, setAssignee] = React.useState<TaskApi.User | null>(() => {
    return task.assignedUser
      ? ({ userName: task.assignedUser, userEmail: task.assignedUserEmail ?? '' } as TaskApi.User)
      : null;
  });

  const groups = backend.roles;

  React.useEffect(() => {
    setRoles(task.assignedRoles ?? []);
    setAssignee(
      task.assignedUser
        ? ({ userName: task.assignedUser, userEmail: task.assignedUserEmail ?? '' } as TaskApi.User)
        : null
    );
  }, [task.assignedRoles, task.assignedUser, task.assignedUserEmail]);

  React.useEffect(() => {
    let cancelled = false;

    backend.persistence.findAllUsers(roles).then((users) => {
      if (!cancelled) setUserList(users ?? []);
    });

    return () => {
      cancelled = true;
    };
  }, [backend.persistence, roles]);

  function handleSetRoles(selectedGroups: TaskApi.Role[]) {
    const roleIds = selectedGroups.map((g) => g.id);
    setRoles(roleIds);
  }

  async function handleSave() {
    await saveTask({
      assignedUser: assignee?.userName ?? '',
      assignedUserEmail: assignee?.userEmail ?? '',
      assignedRoles: roles,
    });
    onClose();
  }

  return (
    <StyledAssigneeRolesEditDialog className={classes.editDialog} open={open} onClose={onClose} maxWidth='md' slots={{ transition: Zoom }}>
      <DialogTitle>
        {intl.formatMessage({ id: 'task.assigneesAndRolesEdit' })}{": "}{task.taskRef ?? 'no task reference id'}
      </DialogTitle>

      <DialogContent>
        <Grid2 container display='flex' alignItems='center'>
          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.assignee' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledAssigneeAutocomplete>
              <TaskCreateAssignee
                userList={userList}
                value={
                  assignee
                    ? (userList.find(u => u.userName === assignee.userName) ?? assignee)
                    : null
                }
                onChange={(user) => setAssignee(user)}
              />
            </StyledAssigneeAutocomplete>
          </Grid2>
          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.assignedRoles' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <EditRoles assignedRoles={roles} groups={groups} acceptNewRoles={handleSetRoles}/>
          </Grid2>
        </Grid2>
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button
          onClick={handleSave}
          disabled={
            !isTaskChanged({
              assignedUser: assignee?.userName ?? '',
              assignedUserEmail: assignee?.userEmail ?? '',
              assignedRoles: roles,
            })
          }
        >
          {intl.formatMessage({ id: 'button.save' })}
        </Button>
      </DialogActions>
    </StyledAssigneeRolesEditDialog>
  )
}

const MUI_NAME = 'AssigneeRolesEditDialog';
const StyledAssigneeRolesEditDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'AssigneeRolesEdit',
  overridesResolver: (_props, styles) => {
    return [
      styles.editDialog
    ];
  },

})(({ theme }) => {

  return {};
})

const StyledAssigneeAutocomplete = styled('div')({
  width: '100%',
  '& .MuiInputBase-root': {
    height: '3.5rem',
  },
  '& .MuiInputBase-input': {
    height: '100%',
    padding: '0 14px',
    display: 'flex',
    alignItems: 'center',
  },
});

const useUtilityClasses = () => {
  const slots = {
    editDialog: ['editDialog'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
