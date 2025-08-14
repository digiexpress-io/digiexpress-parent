import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, Grid2, styled, TextField, Typography, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { useTaskDashboard } from '../eveli-task-composer-v2/EveliTaskDashboardContext';
import { EditRoles } from './EditRoles';
import { IamApi } from '@dxs-ts/eveli-api';
import { useFetch } from '@dxs-ts/envir-fetch';


export interface AssigneeRolesEditDialogProps {
  open: boolean;
  onClose: () => void
}


export const AssigneeRolesEditDialog: React.FC<AssigneeRolesEditDialogProps> = ({ open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const { task, saveTask, isTaskChanged } = useTaskDashboard();
  const [assignee, setAssignee] = React.useState(task.assignedUser);
  const [roles, setRoles] = React.useState(task.assignedRoles ?? []);
  const { groups } = useFetch('$org/groupsList.GET', {});

  function handleSetAssignee(event: React.ChangeEvent<HTMLInputElement>) {
    setAssignee(event.target.value);
  }

  function handleSetRoles(selectedGroups: IamApi.UserGroup[]) {
    const roleIds = selectedGroups.map((g) => g.id);
    setRoles(roleIds);
  }

  async function handleSave() {
    await saveTask({
      assignedUser: assignee,
      assignedRoles: roles
    });
    onClose();
  }

  return (
    <StyledAssigneeRolesEditDialog className={classes.editDialog} open={open} onClose={onClose} maxWidth='md' slots={{ transition: Zoom }}>
      <DialogTitle>
        {intl.formatMessage({ id: 'task.assigneesAndRolesEdit', defaultMessage: 'Edit assigness and roles' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {task.taskRef ?? 'no task reference id'}
      </DialogTitle>

      <DialogContent>
        <Grid2 container display='flex' alignItems='center'>
          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.assignee', defaultMessage: 'Assignee' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField value={assignee} onChange={handleSetAssignee} />
          </Grid2>
          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.assignedRoles', defaultMessage: 'Roles' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <EditRoles assignedRoles={roles} groups={groups} acceptNewRoles={handleSetRoles}/>
          </Grid2>
        </Grid2>
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={handleSave} disabled={!isTaskChanged({ assignedUser: assignee, assignedRoles: roles })}> {intl.formatMessage({ id: 'button.save' })}</Button>
      </DialogActions>
    </StyledAssigneeRolesEditDialog>
  )
}

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px'
  },
  '& .MuiInputBase-multiline': {
    paddingLeft: '0px',
    paddingRight: '0px'
  },
}));



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


const useUtilityClasses = () => {
  const slots = {
    editDialog: ['editDialog'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
