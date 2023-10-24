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

import Fields from './TaskCreateFields';
import Context from 'context';
import { TaskDescriptorImpl } from 'taskdescriptor';


const SplitButtonItem: React.FC<{
  key: string,
  icon: React.ReactNode,
  buttonText: string,
  onClick: () => void
}> = (props) => {
  const { key, icon, buttonText, onClick } = props;

  return (
    <MenuItem key={key} onClick={onClick}>
      {icon}
      <Typography sx={{ ml: 1 }}><FormattedMessage id={buttonText} /></Typography>
    </MenuItem>
  )
}

const SplitButton: React.FC<{ onClose?: () => void }> = ({ onClose }) => {
  const backend = Context.useBackend();
  const org = Context.useOrg();
  const tasks = Context.useTasks();
  const { state, setState } = Context.useTaskEdit();

  const [open, setOpen] = React.useState(false);
  const anchorRef = React.useRef<HTMLDivElement>(null);

  function handleToggle() {
    setOpen(prev => !prev);
  }

  function handleClose() {
    setOpen(false);
    onClose && onClose();
  }

  async function handleCreateAndClose() {
    const createdTask = await backend.task.createTask({
      commandType: 'CreateTask',
      title: state.task.title,
      description: state.task.description,
      status: state.task.status,
      priority: state.task.priority,

      startDate: state.task.startDate,
      dueDate: state.task.dueDate,

      roles: state.task.roles,
      assigneeIds: state.task.assignees,
      reporterId: org.state.iam.userId,

      labels: state.task.labels,
      extensions: state.task.entry.extensions,
      comments: state.task.comments,
      checklist: state.task.checklist
    });
    await tasks.reload()
    handleClose();
  }

  async function handleCreateAndEdit() {
    handleClose();
  }

  return (
    <React.Fragment>
      <ButtonGroup variant="contained" ref={anchorRef}>
        <Button startIcon={<CheckIcon />} onClick={handleCreateAndClose}>
          <Typography><FormattedMessage id='core.taskCreate.button.create'></FormattedMessage></Typography>
        </Button>
        <Button size="small" onClick={handleToggle}>
          <ArrowDropDownIcon />
        </Button>
      </ButtonGroup>
      <Popper
        open={open}
        anchorEl={anchorRef.current}
        transition
        disablePortal
        placement='top-end'
      >
        {({ TransitionProps }) => (
          <Grow
            {...TransitionProps}
            style={{
              transformOrigin: 'center bottom',
            }}
          >
            <Paper>
              <ClickAwayListener onClickAway={handleClose}>
                <MenuList autoFocusItem sx={{ textTransform: 'uppercase' }}>
                  <SplitButtonItem key='createAndClose' icon={<BlockIcon color='error' />} buttonText='core.taskCreate.button.createAndClose' onClick={handleCreateAndClose} />
                  <SplitButtonItem key='createAndEdit' icon={<EditIcon color='warning' />} buttonText='core.taskCreate.button.createAndEdit' onClick={handleCreateAndEdit} />
                  <SplitButtonItem key='cancel' icon={<CancelIcon color='info' />} buttonText='core.taskCreate.button.cancel' onClick={handleClose} />
                </MenuList>
              </ClickAwayListener>
            </Paper>
          </Grow>
        )}
      </Popper>
    </React.Fragment>
  );
}


export default SplitButton;