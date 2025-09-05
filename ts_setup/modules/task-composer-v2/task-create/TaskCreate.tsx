import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, Stack, styled, TextField, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { EditRoles } from './TaskCreateRoles';
import { EditPriority } from './TaskCreatePriority';
import { TaskCreateAssignee } from './TaskCreateAssignee';

interface RequiredError {
  subject?: string;
}

export const TaskCreate: React.FC<{ open: boolean, onClose: () => void}> = ({ open, onClose }) => {
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
  const [errors, setErrors] = React.useState<RequiredError>({
    subject: intl.formatMessage({ id: 'task.composer.error.subject.required', defaultMessage: '* Task subject is required' })
  });


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
    const value = event.target.value;
    setSubject(value);
    setErrors((prev) => ({ ...prev, subject: value.trim() ? undefined : intl.formatMessage({ id: 'task.composer.error.subject.required', defaultMessage: '* Task subject is required' }) }));
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
    <StyledTaskCreate className={classes.root} open={open} onClose={onClose} maxWidth='md'>

      <DialogTitle><Typography variant='h1'>{intl.formatMessage({ id: 'task.composer.create', defaultMessage: 'Create new task' })}</Typography></DialogTitle>
      <DialogContent>

        <Stack direction="row" spacing={3} className={classes.fieldsRow}>
          {/* LEFT COLUMN */}
          <Stack direction="column" spacing={1} flex={1}>
            <Typography fontWeight={500}>
              {intl.formatMessage({ id: 'task.composer.dueDate', defaultMessage: 'Due' })}
            </Typography>
            <backend.slots.DateTimePicker onChange={newDate => setDueDate(newDate)} value={dueDate} />

            <Stack direction="column">
              <Typography fontWeight={500}>
                {intl.formatMessage({ id: 'task.composer.clientName', defaultMessage: 'Client name' })}
              </Typography>
              <StyledTextField onChange={handleSetClientName} value={clientName} />
            </Stack>

            <Stack direction="column">
              <Typography fontWeight={500}>
                {intl.formatMessage({ id: 'task.composer.subject', defaultMessage: '* Task subject' })}
              </Typography>
              <StyledTextField
                required
                onChange={handleSetSubject}
                value={subject}
                placeholder={intl.formatMessage({ id: 'task.composer.field.required', defaultMessage: '* required' })}
                error={!!errors.subject}
                helperText={errors.subject}
              />
            </Stack>

            <Typography fontWeight={500}>
              {intl.formatMessage({ id: 'task.composer.description', defaultMessage: 'Description' })}
            </Typography>
            <StyledTextField multiline maxRows={4} onChange={handleSetDescription} value={description} />

            <Typography fontWeight={500}>
              {intl.formatMessage({ id: 'task.composer.additionalInfo', defaultMessage: 'Additional information' })}
            </Typography>
            <StyledTextField multiline maxRows={4} onChange={handleSetAddInfo} value={addInfo} />
          </Stack>

          {/* RIGHT COLUMN */}

          <Stack direction="column" spacing={2} flex={1} className={classes.colRight}>

            <Stack direction="column">
              <Typography fontWeight={500}>
                {intl.formatMessage({ id: 'task.composer.roles', defaultMessage: 'Roles' })}
              </Typography>
              <EditRoles assignedRoles={roles} groups={groups} acceptNewRoles={handleSetRoles} />
            </Stack>

            <Stack direction="column">
              <Typography fontWeight={500}>
                {intl.formatMessage({ id: 'task.composer.assignee', defaultMessage: 'Assignee' })}
              </Typography>
              <TaskCreateAssignee onChange={(user) => setAssignee(user)} userList={userList} value={assignee} />
            </Stack>

            <Stack direction="column">
              <Typography fontWeight={500}>
                {intl.formatMessage({ id: 'task.composer.priority', defaultMessage: 'Priority' })}
              </Typography>
              <EditPriority onChange={handleSetPriority} priority={priority} />
            </Stack>

            <Stack direction="column" spacing={1}>
              <Typography fontWeight={500}>
                {intl.formatMessage({ id: 'task.composer.status', defaultMessage: 'Status' })}
              </Typography>
              <StyledTextField value={intl.formatMessage({ id: 'task.status.new' })}
                slotProps={{
                  input: {
                    disabled: true
                  },
                }} />
            </Stack>
          </Stack>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={handleCreateTask} disabled={!subject.trim()}>{intl.formatMessage({ id: 'button.accept' })}</Button>
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
    },

    '& .TaskCreate-rowsContainer': {
      width: '100%',
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
    },
    '& .TaskCreate-colRight': {
      backgroundColor: theme.palette.secondary.main,
      padding: theme.spacing(3),
      border: `1px solid ${theme.palette.divider}`,
    }
  };
})

const StyledTextField = styled(TextField)(({ theme }) => ({
  marginTop: theme.spacing(0.5),

  '& .MuiOutlinedInput-root': {
    '&.MuiInputBase-multiline': {
      padding: 0,
      minHeight: '2.5rem',
    },
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
    rowsContainer: ['rowsContainer'],
    colRight: ['colRight']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}