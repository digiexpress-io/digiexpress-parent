import React from 'react';
import { Autocomplete, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, Stack, styled, TextField, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FormattedMessage, useIntl } from 'react-intl';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { EditRoles } from './TaskCreateRoles';
import { EditPriority } from './TaskCreatePriority';

interface RequiredError {
  subject?: string;
}

export const TaskCreate: React.FC = () => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const backend = useTaskBackend();
  const groups = backend.roles;

  const [clientName, setClientName] = React.useState<string>('');
  const [subject, setSubject] = React.useState<string>('');
  const [addInfo, setAddInfo] = React.useState<string>('');
  const [description, setDescription] = React.useState<string>('');
  const [assignee, setAssignee] = React.useState<TaskApi.User>({
    userName: backend.currentUser.name,
    userEmail: backend.currentUser.email
  });
  const [roles, setRoles] = React.useState<string[]>([]);
  const [priority, setPriority] = React.useState<TaskApi.TaskPriority>(TaskApi.TaskPriority.NORMAL);
  const [dueDate, setDueDate] = React.useState<Date | null>(new Date());

  const [userList, setUserList] = React.useState<TaskApi.User[]>([]);
  const [errors, setErrors] = React.useState<RequiredError>({});


  React.useEffect(() => {
    if (roles.length > 0) {
      backend.persistence.findAllUsers(roles).then(setUserList);
    } else {
      setUserList([]);
    }
  }, [roles])


  function handleSetRoles(selectedGroups: TaskApi.Role[]) {
    const roleIds = selectedGroups.map((g) => g.id);
    setRoles(roleIds);
  };
  function handleSetPriority(level: TaskApi.TaskPriority) {
    setPriority(level);
  };
  function handleSetClientName(event: React.ChangeEvent<HTMLInputElement>) {
    setClientName(event.target.value);
  };
  function handleSetSubject(event: React.ChangeEvent<HTMLInputElement>) {
    if (errors.subject) {
      setErrors((prev) => ({ ...prev, subject: undefined }));
    }
    setSubject(event.target.value);
  };
  function handleSetAddInfo(event: React.ChangeEvent<HTMLInputElement>) {
    setAddInfo(event.target.value);
  };
  function handleSetDescription(event: React.ChangeEvent<HTMLInputElement>) {
    setDescription(event.target.value);
  };

  async function handleCreateTask() {
    const newErrors: RequiredError = {};
    if (!subject.trim()) {
      newErrors.subject = 'Task subject is required'
    };
    setErrors(newErrors);

    if (Object.keys(newErrors).length > 0) return;

    const taskFromValues: Partial<TaskApi.Task> = {
      priority: priority,
      subject: subject,
      description: description,
      dueDate: dueDate ?? undefined,
      status: TaskApi.TaskStatus.NEW,
      assignedUser: assignee?.userName ?? '',
      assignedUserEmail: assignee?.userEmail ?? '',
      clientIdentificator: clientName,
      assignedRoles: roles,
      additionalInfo: addInfo,
    }
    backend.persistence.createOneTask(taskFromValues);
  };


  return (
    <StyledTaskCreate className={classes.root} open={true} onClose={() => { }}>

      <DialogTitle><Typography variant='h1'>{intl.formatMessage({ id: 'task.composer.create', defaultMessage: 'Create new task' })}</Typography></DialogTitle>

      <DialogContent>
        <Stack direction='column' spacing={1} className={classes.rowsContainer}>
          <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.dueDate', defaultMessage: 'Due' })}</Typography>
          <backend.slots.DateTimePicker onChange={newDate => setDueDate(newDate)} value={dueDate} />

          <Stack direction='column' flex={1}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.clientName', defaultMessage: 'Client name' })}</Typography>
            <StyledTextField onChange={handleSetClientName} value={clientName} />
          </Stack>

          <Stack direction='column' flex={1}>
            <Typography fontWeight={500}>
              {intl.formatMessage({ id: 'task.composer.subject', defaultMessage: '* Task subject' })}
            </Typography>
            <StyledTextField required onChange={handleSetSubject} value={subject}
              placeholder={intl.formatMessage({ id: 'task.composer.field.required', defaultMessage: '* required field' })}
              error={!!errors.subject}
              helperText={errors.subject}
            />
          </Stack>

          <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.additionalInfo', defaultMessage: 'Additional information' })}</Typography>
          <StyledTextField multiline minRows={2} maxRows={4} onChange={handleSetAddInfo} value={addInfo} />


          <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.description', defaultMessage: 'Description' })}</Typography>
          <StyledTextField multiline minRows={2} maxRows={4} onChange={handleSetDescription} value={description} />
        </Stack>

        <Stack spacing={1}>
          <Stack direction='column' flex={1}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.roles', defaultMessage: 'Roles' })}</Typography>
            <EditRoles assignedRoles={roles} groups={groups} acceptNewRoles={handleSetRoles} />
          </Stack>

          <Stack direction='column' flex={1}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'task.composer.assignee', defaultMessage: 'Assignee' })}</Typography>

            <Autocomplete
              id="assignedUser"
              freeSolo
              options={userList}
              getOptionLabel={option => (typeof option === "string") ? option : option.userName ?? ''}
              value={assignee}
              onInputChange={(_event, newInputValue) => {
                if (newInputValue === assignee.userName) {
                  return;
                }
                setAssignee(userList.find(el => el.userName === newInputValue)!);
              }}
              renderInput={(params) => (
                <TextField {...params}
                  name='assignedUser'
                  fullWidth={true}
                  label={<FormattedMessage id='taskDialog.assignedUser' />}
                  InputLabelProps={{
                    shrink: true,
                  }}
                />
              )}
            />
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
      <DialogActions>
        <Button variant='outlined' onClick={handleCreateTask}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={handleCreateTask}>{intl.formatMessage({ id: 'button.accept' })}</Button>
      </DialogActions>
    </StyledTaskCreate>
  )
}

const MUI_NAME = 'TaskCreate';
const StyledTaskCreate = styled(Dialog, {
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
    padding: '0 12px',
    '&::placeholder': {
      color: theme.palette.error.main,
      ...theme.typography.subtitle2,
      opacity: 1
    },
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