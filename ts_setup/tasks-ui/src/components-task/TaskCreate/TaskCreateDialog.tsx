import React from 'react';
import { Dialog, DialogContent, DialogTitle, Stack, Box, DialogActions, IconButton, Typography, alpha } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FormattedMessage } from 'react-intl';

import TaskCreateActions from './TaskCreateActions';
import Fields from './TaskCreateFields';
import { Task, UserProfileAndOrg } from 'client';
import Context from 'context';
import Burger from 'components-burger';
import { TaskEditProvider } from 'descriptor-task';
import { sambucus, wash_me } from 'components-colors';

function initTaskProps(userId: string): Task {
  return {
    id: '',
    title: 'new task title ',
    description: 'new task description',
    status: 'CREATED',
    priority: 'MEDIUM',

    startDate: new Date().toISOString(),
    dueDate: new Date().toISOString(),

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

  if (!props.open) {
    return null;
  }

  return (
    <TaskEditProvider task={initTaskProps(org.state.iam.userId)}>
      <Dialog open={true} fullWidth maxWidth='md'>
        <DialogTitle sx={{
          backgroundColor: wash_me,
          borderBottom: `1px solid ${alpha(sambucus, 0.3)}`,
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
            <Burger.Section>
              <Typography fontWeight='bold'><FormattedMessage id='core.taskCreate.fields.title' /></Typography>
              <Fields.Title />
            </Burger.Section>

            <Burger.Section>
              <Typography fontWeight='bold'><FormattedMessage id='core.taskCreate.fields.description' /></Typography>
              <Fields.Description />
            </Burger.Section>

            <Stack spacing={1} direction='row'>
              <Burger.Section>
                <Typography fontWeight='bold'><FormattedMessage id='core.taskCreate.fields.startDate' /></Typography>
                <Fields.StartDate />
              </Burger.Section>
              <Burger.Section>
                <Typography fontWeight='bold'><FormattedMessage id='core.taskCreate.fields.dueDate' /></Typography>
                <Fields.DueDate />
              </Burger.Section>
            </Stack>

            <Stack spacing={1} direction='row'>
              <Burger.Section>
                <Typography fontWeight='bold'><FormattedMessage id='core.taskCreate.fields.status' /></Typography>
                <Fields.Status />
              </Burger.Section>
              <Burger.Section>
                <Typography fontWeight='bold'><FormattedMessage id='core.taskCreate.fields.priority' /></Typography>
                <Fields.Priority />
              </Burger.Section>
            </Stack>

            <Stack spacing={1} direction='row'>
              <Burger.Section>
                <Typography fontWeight='bold'><FormattedMessage id='core.taskCreate.fields.assignees' /></Typography>
                <Fields.Assignees />
              </Burger.Section>
              <Burger.Section>
                <Typography fontWeight='bold'><FormattedMessage id='core.taskCreate.fields.roles' /></Typography>
                <Fields.Roles />
              </Burger.Section>
            </Stack>

          </Stack>
        </DialogContent>
        <DialogActions>
          <TaskCreateActions onClose={props.onClose} />
        </DialogActions>
      </Dialog>
    </TaskEditProvider>
  );
}

export default TaskCreateDialog;