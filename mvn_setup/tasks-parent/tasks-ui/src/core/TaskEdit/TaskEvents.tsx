import React from 'react';
import { ListItem, ListItemText, Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import Context from 'context';
import TimestampFormatter from '../TimestampFormatter';

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
      <Box /><Typography><FormattedMessage id='task.history.createTask' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }

  if (event.body.commandType === "AssignTask") {
    return (<>
      <Typography><FormattedMessage id='task.history.assignTask' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "AssignTaskRoles") {
    return (<>
      <Typography><FormattedMessage id='task.history.assignTaskRoles' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeTaskStatus") {
    return (<>
      <Typography><FormattedMessage id='task.history.changeTaskStatus' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeTaskPriority") {
    return (<>
      <Typography><FormattedMessage id='task.history.changeTaskPriority' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeTaskInfo") {
    return (<>
      <Typography><FormattedMessage id='task.history.changeTaskInfo' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeTaskStartDate") {
    return (<>
      <Typography><FormattedMessage id='task.history.changeTaskStartDate' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeTaskDueDate") {
    return (<>
      <Typography><FormattedMessage id='task.history.changeTaskDueDate' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeChecklistItemAssignees") {
    return (<>
      <Typography><FormattedMessage id='task.history.changeChecklistItemAssignees' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeChecklistItemDueDate") {
    return (<>
      <Typography><FormattedMessage id='task.history.changeChecklistItemDueDate' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeChecklistTitle") {
    return (<>
      <Typography><FormattedMessage id='task.history.changeChecklistTitle' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeChecklistItemTitle") {
    return (<>
      <Typography><FormattedMessage id='task.history.ChangeChecklistItemTitle' /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
    </>)
  }
  if (event.body.commandType === "ChangeChecklistItemCompleted") {
    const msg = event.body.toCommand.completed === true ? 'task.history.ChangeChecklistItemCompleted' : 'task.history.ChangeChecklistItemNotCompleted';

    console.log("toCommand ", event.body.toCommand.completed + '.....' + event.body.toCommand.checklistItemId)

    return (<>
      <Typography><FormattedMessage id={msg} /></Typography>
      <Box flexGrow={1} />
      <TimestampFormatter type='dateTime' value={date} />
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