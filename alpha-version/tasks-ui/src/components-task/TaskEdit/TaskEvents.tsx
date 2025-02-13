import React from 'react';
import { ListItem, Typography, Box, ListItemText, Grid } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import Burger from 'components-burger';
import { CollapsedEvent, SingleEvent, TaskEditEvent } from 'descriptor-task';
import { TaskEventDiff } from './TaskEventDiff';


const CollapsedGroup: React.FC<{ event: CollapsedEvent }> = () => {
  return (<>19 items collapsed</>)
}

const SingleGroup: React.FC<{ event: SingleEvent }> = ({ event }) => {


  if (!event.body.toCommand.targetDate) {
    return <></>;
  }

  const date = new Date(event.body.toCommand.targetDate);



  if (event.body.commandType === "CreateTask") {
    return (
      <Grid container>
        <Grid item md={9} lg={9}>
          <Typography fontWeight='bolder'><FormattedMessage id='task.event.createTask' /></Typography>
        </Grid>
        <Grid item md={3} lg={3}>
          <Typography textAlign='right'><Burger.DateTimeFormatter type='dateTime' value={date} /></Typography>
        </Grid>
      </Grid>)
  }

  if (event.body.commandType === "AssignTask") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.assignTask'
        fromValue={event.body.fromCommand?.assigneeIds} toValue={event.body.toCommand.assigneeIds} />
    )
  }
  if (event.body.commandType === "AssignTaskRoles") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.assignTaskRoles'
        fromValue={event.body.fromCommand?.roles} toValue={event.body.toCommand.roles} />
    )
  }
  if (event.body.commandType === "ChangeTaskStatus") {
    return (<TaskEventDiff event={event} eventTitle='task.event.assignTask'
      fromValue={event.body.fromCommand?.status} toValue={event.body.toCommand.status} />
    )
  }
  if (event.body.commandType === "ChangeTaskPriority") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.changeTaskPriority'
        fromValue={event.body.fromCommand?.priority} toValue={event.body.toCommand.priority} />
    )
  }
  //TODO clean up changeTaskInfo command
  if (event.body.commandType === "ChangeTaskInfo") {
    if (event.body.toCommand.title) {
      <TaskEventDiff event={event} eventTitle='task.event.changeTaskTitle'
        fromValue={event.body.fromCommand?.title} toValue={event.body.toCommand.title} />
    }
    return (
      <TaskEventDiff event={event} eventTitle='task.event.changeTaskDescription'
        fromValue={event.body.fromCommand?.description} toValue={event.body.toCommand.description} />
    )
  }
  //TODO DateTime formatting
  if (event.body.commandType === "ChangeTaskStartDate") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.changeTaskStartDate'
        fromValue={event.body.fromCommand?.startDate} toValue={event.body.toCommand.startDate} />
    )
  }
  //TODO DateTime formatting
  if (event.body.commandType === "ChangeTaskDueDate") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.changeTaskDueDate'
        fromValue={event.body.fromCommand?.dueDate} toValue={event.body.toCommand.dueDate} />
    )
  }
  if (event.body.commandType === "ChangeChecklistItemAssignees") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.changeChecklistItemAssignees'
        fromValue={event.body.fromCommand?.assigneeIds} toValue={event.body.toCommand.assigneeIds} />
    )
  }
  //TODO DateTime formatting
  if (event.body.commandType === "ChangeChecklistItemDueDate") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.changeChecklistItemDueDate'
        fromValue={event.body.fromCommand?.dueDate} toValue={event.body.toCommand.dueDate} />
    )
  }
  if (event.body.commandType === "ChangeChecklistTitle") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.changeChecklistTitle'
        fromValue={event.body.fromCommand?.title} toValue={event.body.toCommand.title} />
    )
  }
  if (event.body.commandType === "ChangeChecklistItemTitle") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.changeChecklistItemTitle'
        fromValue={event.body.fromCommand?.title} toValue={event.body.toCommand.title} />
    )
  }
  if (event.body.commandType === "ChangeChecklistItemCompleted") {
    const msg = event.body.toCommand.completed === true ? 'task.event.changeChecklistItemCompleted' : 'task.event.changeChecklistItemNotCompleted';
    return (
      <TaskEventDiff event={event} eventTitle={msg}
        fromValue={event.body.fromCommand?.completed.toString()} toValue={event.body.toCommand.completed.toString()} />)
  }

  if (event.body.commandType === "CreateChecklist") {
    return (
      <TaskEventDiff event={event} eventTitle={'task.event.createChecklist'}
        fromValue={event.body.fromCommand?.title} toValue={event.body.toCommand.title} />
    )
  }

  //TODO Checklist title, not ID
  if (event.body.commandType === "DeleteChecklist") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.deleteChecklist'
        fromValue={event.body.fromCommand?.checklistId} toValue={event.body.toCommand.checklistId} />
    )
  }

  if (event.body.commandType === "AddChecklistItem") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.addChecklistItem'
        fromValue={event.body.fromCommand?.title} toValue={event.body.toCommand.title} />
    )
  }

  //TODO Checklist item title, not ID
  if (event.body.commandType === "DeleteChecklistItem") {
    return (
      <TaskEventDiff event={event} eventTitle='task.event.deleteChecklistItem'
        fromValue={event.body.fromCommand?.checklistItemId} toValue={event.body.toCommand.checklistItemId} />
    )
  }

  return (<Box display='flex'><ListItemText primary={event.body.commandType} secondary={event.body.toCommand.targetDate} /></Box>)
}


const Event: React.FC<{ event: TaskEditEvent }> = ({ event }) => {


  if (event.type === 'SINGLE') {
    return <Grid container><SingleGroup event={event} /></Grid>
  }
  return <ListItem><CollapsedGroup event={event} /></ListItem>;
}


export default Event;