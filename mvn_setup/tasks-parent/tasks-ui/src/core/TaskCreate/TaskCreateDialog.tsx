import React from 'react';
import {
  Dialog, DialogContent, DialogTitle, Stack, Box, DialogActions, IconButton, MenuItem, Typography, Button, ButtonGroup,
  Grow, ClickAwayListener, MenuList, Paper, Popper
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import CancelIcon from '@mui/icons-material/Cancel';
import CheckIcon from '@mui/icons-material/Check';
import BlockIcon from '@mui/icons-material/Block';
import EditIcon from '@mui/icons-material/Edit';
import { FormattedMessage } from 'react-intl';

import TaskCreateActions from './TaskCreateActions';
import Fields from './TaskCreateFields';
import { Task } from 'taskclient';
import Context from 'context';
import { TaskDescriptorImpl } from 'taskdescriptor';

function initTaskProps(userId: string): Task {
  return {
    id: '',
    title: 'init task thing',
    description: '',
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

  if (!props.open) {
    return null;
  }

  const init = new TaskDescriptorImpl(initTaskProps(org.state.iam.userId), tasks.state.profile, new Date());

  return (
    <Context.EditProvider task={init}>
      <Dialog open={true} fullWidth maxWidth='md'>
        <DialogTitle>
          <Box display='flex' alignItems='center'>
            <FormattedMessage id='core.taskCreate.newTask' />
            <Box flexGrow={1} />
            <IconButton onClick={props.onClose}>
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>

        <DialogContent>
          <Stack overflow='auto' spacing={1}>
            <Fields.Title />
            <Fields.Description />
            <Fields.Status />
            <Fields.Priority />
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