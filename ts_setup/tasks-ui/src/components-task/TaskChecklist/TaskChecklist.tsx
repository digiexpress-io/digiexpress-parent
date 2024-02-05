import React from 'react';
import { Box, Typography, IconButton, TextField, Checkbox, Stack, Grid, Divider, Tooltip, Button } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import { FormattedMessage } from 'react-intl';

import Context from 'context';
import { 
  TaskUpdateCommand, ChangeChecklistItemCompleted, ChangeChecklistTitle,
  DeleteChecklistItem, ChangeChecklistItemTitle, ChangeChecklistItemDueDate,
  ChangeChecklistItemAssignees,
  DeleteChecklist,
  AddChecklistItem,
  CreateChecklist, 
} from 'descriptor-task';

import Burger from 'components-burger';
import { cyan, cyan_mud } from 'components-colors';

import TaskAssignees from '../TaskAssignees';
import DueDate from '../TaskDueDate';



const TaskChecklist: React.FC<{ onChange: (commands: TaskUpdateCommand<any>[]) => Promise<void> }> = ({ onChange }) => {
  const ctx = Context.useTaskEdit();
  const backend = Context.useTasks();

  async function handleChecklistItemCompleted(checklistId: string, checklistItemId: string, event: React.ChangeEvent<HTMLInputElement>) {

    const command: ChangeChecklistItemCompleted = {
      commandType: 'ChangeChecklistItemCompleted',
      checklistId,
      checklistItemId,
      taskId: ctx.task.id,
      completed: event.target.checked
    };
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  async function handleChecklistTitleChange(checklistId: string, checklistTitle: string) {
    const command: ChangeChecklistTitle = {
      commandType: 'ChangeChecklistTitle',
      checklistId,
      taskId: ctx.task.id,
      title: checklistTitle
    };
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  async function handleChecklistItemTitleChange(checklistId: string, checklistItemId: string, checklistItemTitle: string) {
    const command: ChangeChecklistItemTitle = {
      commandType: 'ChangeChecklistItemTitle',
      checklistId,
      checklistItemId,
      taskId: ctx.task.id,
      title: checklistItemTitle
    };
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  async function handleDueDateChange(dueDate: string | undefined, checklistId: string, checklistItemId: string) {
    const command: ChangeChecklistItemDueDate = {
      checklistId,
      checklistItemId,
      commandType: 'ChangeChecklistItemDueDate',
      dueDate,
      taskId: ctx.task.id
    };
    onChange([command]);
  }

  async function handleAssigneesChange(assigneeIds: string[], checklistId: string, checklistItemId: string) {
    const command: ChangeChecklistItemAssignees = {
      assigneeIds,
      checklistId,
      checklistItemId,
      commandType: 'ChangeChecklistItemAssignees',
      taskId: ctx.task.id
    };
    onChange([command]);
  }

  async function handleChecklistCreate() {
    const command: CreateChecklist = {
      commandType: 'CreateChecklist',
      taskId: ctx.task.id,
      title: "New checklist title",
      checklist: [{ id: '', assigneeIds: [], completed: false, dueDate: undefined, title: 'new to-do item' }]
    };
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  async function handleChecklistItemCreate(checklistId: string) {
    const command: AddChecklistItem = {
      commandType: 'AddChecklistItem',
      taskId: ctx.task.id,
      checklistId,
      completed: false,
      assigneeIds: [],
      dueDate: undefined,
      title: "New to-do item",
    };
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }


  async function handleChecklistDelete(checklistId: string) {
    const command: DeleteChecklist = {
      commandType: 'DeleteChecklist',
      taskId: ctx.task.id,
      checklistId
    };
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  async function handleChecklisItemDelete(checklistId: string, checklistItemId: string) {
    const command: DeleteChecklistItem = {
      commandType: 'DeleteChecklistItem',
      taskId: ctx.task.id,
      checklistItemId,
      checklistId
    };
    const updatedTask = await backend.updateActiveTask(ctx.task.id, [command]);
    ctx.withTask(updatedTask);
  }

  if (!ctx.task.checklist.length) {
    return (
      <Box display='flex' alignItems='center' justifyContent='flex-end' paddingRight={2}>
        <Button startIcon={<AddIcon sx={{ color: cyan }} />} onClick={handleChecklistCreate}>
          <Typography sx={{ fontWeight: 'bold', color: 'text.primary', textTransform: 'capitalize' }}><FormattedMessage id='task.checklist.add' /></Typography>
        </Button>
      </Box>
    )
  }

  return (<>
    <Box display='flex' alignItems='center' justifyContent='space-between' paddingRight={2} paddingLeft={2.5}>
      <Typography fontWeight='bold'><FormattedMessage id='task.checklist' /></Typography>
      <Button startIcon={<AddIcon sx={{ color: cyan }} />} onClick={handleChecklistCreate}>
        <Typography sx={{ fontWeight: 'bold', color: 'text.primary', textTransform: 'capitalize' }}><FormattedMessage id='task.checklist.add' /></Typography>
      </Button>
    </Box>

    {/*  checklist section  */}

    {ctx.task.checklist.map((checklist, index) => (<Burger.Section key={index} width='95%'>
      <Box>
        <TextField defaultValue={checklist.title}
          onBlur={(event) => handleChecklistTitleChange(checklist.id, event.currentTarget.value)}
          fullWidth
          InputProps={{
            endAdornment: (<IconButton onClick={() => { handleChecklistDelete(checklist.id) }}><DeleteForeverIcon color='error' fontSize='small' /></IconButton>),
            sx: {
              height: '2.5rem',
              pr: 0,
              fontWeight: 'bold',
              backgroundColor: cyan_mud
            }
          }}
        />
      </Box>

      <Stack spacing={1}>
        <Grid container key={index} id={checklist.id} direction='row' alignItems='center'
          sx={{
            color: 'text.primary',
            borderRadius: 1,
            width: '100%'
          }}>
        </Grid>

        {/* checklist items section  */}

        <Box display='flex' alignItems='center' justifyContent='flex-end' >
          <Button startIcon={<AddIcon sx={{ color: cyan }} />} onClick={() => handleChecklistItemCreate(checklist.id)}>
            <Typography sx={{ color: 'text.primary', textTransform: 'capitalize' }}>
              <FormattedMessage id='task.checklistItem.add' />
            </Typography>
          </Button>
        </Box>

        {checklist.items.map((item, index) => (<Box key={index}>

          <Grid container alignItems='center' key={index}>
            <Grid item md={2} lg={2}>
              <Typography fontWeight='bolder'><FormattedMessage id='task.checklist.item.title' /></Typography>
            </Grid>

            <Grid item md={10} lg={10} alignItems='center'>
              <Box display='flex' alignItems='center'>
                <Tooltip placement='top' arrow title={<FormattedMessage id='core.taskEdit.fields.checklistItem.tooltip.markCompleted' />}>
                  <Checkbox size='medium' sx={{ p: 0, mr: 1, '& .MuiSvgIcon-root': { color: cyan } }}
                    onChange={(event) => handleChecklistItemCompleted(checklist.id, item.id, event)}
                    checked={item.completed}
                  />
                </Tooltip>
                <TextField
                  defaultValue={item.title}
                  disabled={item.completed}
                  fullWidth
                  InputProps={{
                    sx: { height: '2.5rem', pr: 0 },
                    endAdornment: (<IconButton onClick={() => { handleChecklisItemDelete(checklist.id, item.id) }}>
                      <DeleteForeverIcon color='error' fontSize='small' />
                    </IconButton>),
                  }}
                  onBlur={(event) => handleChecklistItemTitleChange(checklist.id, item.id, event.currentTarget.value)}
                />
              </Box>
            </Grid>
          </Grid>

          <Grid container alignItems='center' >

            <Grid item md={2} lg={2}>
              <Typography fontWeight='bolder'><FormattedMessage id='task.checklist.item.dueDate' /></Typography>
            </Grid>
            <Grid item md={10} lg={10}>
              <DueDate
                disabled={item.completed}
                task={{ dueDate: item.dueDate ? new Date(item.dueDate) : undefined }}
                onChange={(dueDate) => handleDueDateChange(dueDate, checklist.id, item.id)} />
            </Grid>
          </Grid>
          <Grid container>
            <Grid item md={2} lg={2} alignSelf='center'>
              <Typography fontWeight='bolder'><FormattedMessage id='task.checklist.item.assignees' /></Typography>
            </Grid>
            <Grid item md={10} lg={10}>
              <TaskAssignees disabled={item.completed}
                onChange={(users) => handleAssigneesChange(users, checklist.id, item.id)} task={{ assignees: item.assigneeIds }} />
            </Grid>
          </Grid>

          {checklist.items.length - 1 === index ? undefined : <Divider />}
        </Box>

        ))}



      </Stack>

      {/* Add new checklist item  */}

      < Box display='flex' alignItems='center' justifyContent='flex-end' key={index} >
        <Button startIcon={<AddIcon sx={{ color: cyan }} />} onClick={() => handleChecklistItemCreate(checklist.id)}>
          <Typography sx={{ color: 'text.primary', textTransform: 'capitalize' }}>
            <FormattedMessage id='task.checklistItem.add' />
          </Typography>
        </Button>
      </Box >
    </Burger.Section >
    ))
    }
  </>)
}
export default TaskChecklist;