import React from 'react';
import { Box, DialogContent, DialogTitle, generateUtilityClass, Stack, styled, TextField, Typography } from '@mui/material';

import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { DatePicker } from '@dxs-ts/xui-datetime';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { EditRoles } from './TaskCreateRoles';
import { EditPriority } from './TaskCreatePriority';


export const TaskCreate: React.FC = () => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const [roles, setRoles] = React.useState<string[]>([]);
  const [priority, setPriority] = React.useState<TaskApi.TaskPriority>(TaskApi.TaskPriority.NORMAL);

  const backend = useTaskBackend();
  const groups = backend.roles;


  function handleSetRoles(selectedGroups: TaskApi.Role[]) {
    const roleIds = selectedGroups.map((g) => g.id);
    setRoles(roleIds);
  }

  function handleSetPriority(level: TaskApi.TaskPriority) {
      setPriority(level);
    };
  


  return (
    <StyledTaskCreate className={classes.root}>

      <DialogTitle><Typography variant='h1'>{intl.formatMessage({ id: 'task.composer.create', defaultMessage: 'Create new task' })}</Typography></DialogTitle>

      <DialogContent>
        <Stack direction='column' spacing={1} className={classes.rowsContainer}>
          <>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.dueDate', defaultMessage: 'Due' })}</Typography>
            <DatePicker
              //value={currentState.dueDate}
              //onChange={newDate => setFieldValue('dueDate', newDate)}
              value={new Date()}
              onChange={() => { }}
              inline={false}
            />
          </>

          <Stack direction='column' flex={1}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.clientName', defaultMessage: 'Client name' })}</Typography>
            <StyledTextField />
          </Stack>

          <Stack direction='column' flex={1}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.subject', defaultMessage: 'Task subject' })}</Typography>
            <StyledTextField required />
          </Stack>

          <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.additionalInfo', defaultMessage: 'Additional information' })}</Typography>
          <StyledTextField multiline minRows={2} maxRows={4} />
        </Stack>

        <Stack spacing={1}>
          <Stack direction='column' flex={1}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.roles', defaultMessage: 'Roles' })}</Typography>
            <EditRoles assignedRoles={roles} groups={groups} acceptNewRoles={handleSetRoles} />
          </Stack>

          <Stack direction='column' flex={1}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.assignee', defaultMessage: 'Assignee' })}</Typography>
            <StyledTextField required />
          </Stack>
        </Stack>

        <Stack direction='column' flex={1} className={classes.rowsContainer}>
          <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.priority', defaultMessage: 'Priority' })}</Typography>
          <EditPriority onChange={handleSetPriority} priority={priority} />
        </Stack>

        <Stack direction='column' spacing={1} className={classes.rowsContainer}>
          <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.status', defaultMessage: 'Status' })}</Typography>
          <StyledTextField value={intl.formatMessage({ id: 'task.status.new' })} disabled />
        </Stack>
      </DialogContent>
    </StyledTaskCreate>
  )
}






const MUI_NAME = 'TaskCreate';
const StyledTaskCreate = styled('div', {
  name: MUI_NAME,
  slot: 'TaskCreateView',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1),

    '& .MuiDivider-root': {
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
    },
    '& .TaskCreate-fieldsRow': {
      display: 'flex',
      justifyContent: 'space-between',
    },

    '& .TaskCreate-rowsContainer': {
      width: '100%',
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
    },


  };
})

const StyledTextField = styled(TextField)(({ theme }) => ({
  marginTop: 0,

  '& .MuiOutlinedInput-root': {
  },
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px'
  },
}));

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    fieldsRow: ['fieldsRow'],
    rowsContainer: ['rowsContainer']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}