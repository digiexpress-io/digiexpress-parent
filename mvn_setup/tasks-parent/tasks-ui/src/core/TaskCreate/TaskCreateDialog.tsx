import React from 'react';
import { Dialog, DialogContent, DialogTitle, Stack, Box, DialogActions, IconButton, Typography, useTheme, alpha } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage } from 'react-intl';

import TaskCreateActions from './TaskCreateActions';
import Fields from './TaskCreateFields';
import { Task } from 'taskclient';
import Context from 'context';
import { TaskDescriptorImpl } from 'taskdescriptor';

function initTaskProps(userId: string): Task {
  return {
    id: '',
    title: 'task title ',
    description: 'task description',
    status: 'CREATED',
    priority: 'MEDIUM',

    startDate: undefined,
    dueDate: undefined,

    roles: [],
    assigneeIds: [userId],
    reporterId: userId,

    labels: [],
    extensions: [],
    comments: [],
    checklist: [],
    parentId: undefined,
    transactions: [],

    archived: undefined,
    created: '',
    updated: '',
    version: '',
    documentType: 'TASK'
  }
}

const TaskCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = (props) => {
  const org = Context.useOrg();
  const tasks = Context.useTasks();
  const theme = useTheme();

  if (!props.open) {
    return null;
  }

  const init = new TaskDescriptorImpl(initTaskProps(org.state.iam.userId), tasks.state.profile, new Date());

  return (
    <Context.EditProvider task={init}>
      <Dialog open={true} fullWidth maxWidth='lg'>
        <DialogTitle sx={{
          backgroundColor: theme.palette.mainContent.main,
          borderBottom: `1px solid ${alpha(theme.palette.mainContent.dark, 0.3)}`,
          mb: 1,
        }}>
          <Box display='flex' alignItems='center'>
            <Typography variant='h4' fontWeight='bolder'><FormattedMessage id='core.taskCreate.newTask' /></Typography>
            <Box flexGrow={1} />
            <IconButton onClick={props.onClose}>
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>

        <DialogContent>
          <Stack overflow='auto' spacing={1} direction='column'>
            <Fields.Title />
            <Fields.StartDate onClick={() => { }} />
            <Fields.DueDate onClick={() => { }} />
            <Fields.Status />
            <Fields.Priority />
            <Fields.Assignees />
            <Fields.Roles />
            <Fields.Description />
          </Stack>
        </DialogContent>
        <DialogActions>
          <TaskCreateActions onClose={props.onClose} />
        </DialogActions>
      </Dialog>
    </Context.EditProvider>
  );
}

export default TaskCreateDialog;