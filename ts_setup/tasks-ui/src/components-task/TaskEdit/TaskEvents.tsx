import React from 'react';
import { ListItem, ListItemText, Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import Context from 'context';
import Burger from 'components-burger';

const CollapsedGroup: React.FC<{ event: Context.CollapsedEvent }> = () => {
  return (<>19 items collapsed</>)
}

const SingleGroup: React.FC<{ event: Context.SingleEvent }> = ({ event }) => {
  const { state } = Context.useTaskEdit();

  if (!event.body.toCommand.targetDate) {
    return <></>;
  }


  const date = new Date(event.body.toCommand.targetDate)

  if (event.body.commandType === "CreateTask") {
    return (<>
      <Box /><Typography><FormattedMessage id='task.event.createTask' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }

  if (event.body.commandType === "AssignTask") {
    return (<>
      <Typography><FormattedMessage id='task.event.assignTask' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "AssignTaskRoles") {
    return (<>
      <Typography><FormattedMessage id='task.event.assignTaskRoles' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeTaskStatus") {
    return (<>
      <Typography><FormattedMessage id='task.event.changeTaskStatus' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeTaskPriority") {
    return (<>
      <Typography><FormattedMessage id='task.event.changeTaskPriority' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeTaskInfo") {
    return (<>
      <Typography><FormattedMessage id='task.event.changeTaskInfo' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeTaskStartDate") {
    return (<>
      <Typography><FormattedMessage id='task.event.changeTaskStartDate' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeTaskDueDate") {
    return (<>
      <Typography><FormattedMessage id='task.event.changeTaskDueDate' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeChecklistItemAssignees") {
    return (<>
      <Typography><FormattedMessage id='task.event.changeChecklistItemAssignees' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeChecklistItemDueDate") {
    return (<>
      <Typography><FormattedMessage id='task.event.changeChecklistItemDueDate' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeChecklistTitle") {
    return (<>
      <Typography><FormattedMessage id='task.event.changeChecklistTitle' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeChecklistItemTitle") {
    return (<>
      <Typography><FormattedMessage id='task.event.changeChecklistItemTitle' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeChecklistItemCompleted") {
    const msg = event.body.toCommand.completed === true ? 'task.event.changeChecklistItemCompleted' : 'task.event.changeChecklistItemNotCompleted';
    return (<>
      <Typography><FormattedMessage id={msg} /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "CreateChecklist") {
    return (<>
      <Typography><FormattedMessage id='task.event.createChecklist' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "DeleteChecklist") {
    return (<>
      <Typography><FormattedMessage id='task.event.deleteChecklist' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "AddChecklistItem") {
    return (<>
      <Typography><FormattedMessage id='task.event.addChecklistItem' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "DeleteChecklistItem") {
    return (<>
      <Typography><FormattedMessage id='task.event.deleteChecklistItem' /></Typography>
      <Box flexGrow={1} />
      <Burger.DateTimeFormatter type='dateTime' value={date} />
    </>)
  }

  return (<Box display='flex'><ListItemText primary={event.body.commandType} secondary={event.body.toCommand.targetDate} /></Box>)
}


const Event: React.FC<{ event: Context.TaskEditEvent }> = ({ event }) => {

  if (event.type === 'SINGLE') {
    return <ListItem><SingleGroup event={event} /></ListItem>
  }
  return <ListItem><CollapsedGroup event={event} /></ListItem>;
}


export default Event;