import React from 'react';
import { Box, Typography, IconButton, TextField, Checkbox, Stack, Grid, Divider, Tooltip, Button } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import { FormattedMessage } from 'react-intl';
import Context from 'context';
import Client from 'client';
import TaskAssignees from '../TaskAssignees';
import DueDate from '../TaskDueDate';
import Burger from 'components-burger';

const TaskChecklist: React.FC<{ onChange: (commands: Client.TaskUpdateCommand<any>[]) => Promise<void> }> = ({ onChange }) => {
  const { state, setState } = Context.useTaskEdit();
  const backend = Context.useBackend();

  async function handleChecklistItemCompleted(checklistId: string, checklistItemId: string, event: React.ChangeEvent<HTMLInputElement>) {

    const command: Client.ChangeChecklistItemCompleted = {
      commandType: 'ChangeChecklistItemCompleted',
      checklistId,
      checklistItemId,
      taskId: state.task.id,
      completed: event.target.checked
    };
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  async function handleChecklistTitleChange(checklistId: string, checklistTitle: string) {
    const command: Client.ChangeChecklistTitle = {
      commandType: 'ChangeChecklistTitle',
      checklistId,
      taskId: state.task.id,
      title: checklistTitle
    };
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  async function handleChecklistItemTitleChange(checklistId: string, checklistItemId: string, checklistItemTitle: string) {
    const command: Client.ChangeChecklistItemTitle = {
      commandType: 'ChangeChecklistItemTitle',
      checklistId,
      checklistItemId,
      taskId: state.task.id,
      title: checklistItemTitle
    };
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  async function handleDueDateChange(dueDate: string | undefined, checklistId: string, checklistItemId: string) {
    const command: Client.ChangeChecklistItemDueDate = {
      checklistId,
      checklistItemId,
      commandType: 'ChangeChecklistItemDueDate',
      dueDate,
      taskId: state.task.id
    };
    onChange([command]);
  }

  async function handleAssigneesChange(assigneeIds: string[], checklistId: string, checklistItemId: string) {
    const command: Client.ChangeChecklistItemAssignees = {
      assigneeIds,
      checklistId,
      checklistItemId,
      commandType: 'ChangeChecklistItemAssignees',
      taskId: state.task.id
    };
    onChange([command]);
  }

  async function handleChecklistCreate() {
    const command: Client.CreateChecklist = {
      commandType: 'CreateChecklist',
      taskId: state.task.id,
      title: "New checklist title",
      checklist: [{ id: '', assigneeIds: [], completed: false, dueDate: undefined, title: 'new to-do item' }]
    };
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  async function handleChecklistItemCreate(checklistId: string) {
    const command: Client.AddChecklistItem = {
      commandType: 'AddChecklistItem',
      taskId: state.task.id,
      checklistId,
      completed: false,
      assigneeIds: [],
      dueDate: undefined,
      title: "New to-do item",
    };
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }


  async function handleChecklistDelete(checklistId: string) {
    const command: Client.DeleteChecklist = {
      commandType: 'DeleteChecklist',
      taskId: state.task.id,
      checklistId
    };
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  async function handleChecklisItemDelete(checklistId: string, checklistItemId: string) {
    const command: Client.DeleteChecklistItem = {
      commandType: 'DeleteChecklistItem',
      taskId: state.task.id,
      checklistItemId,
      checklistId
    };
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  if (!state.task.checklist.length) {
    return (
      <Box display='flex' alignItems='center' justifyContent='flex-end' paddingRight={2}>
        <Button startIcon={<AddIcon sx={{ color: 'uiElements.main' }} />} onClick={handleChecklistCreate}>
          <Typography sx={{ fontWeight: 'bold', color: 'text.primary', textTransform: 'capitalize' }}><FormattedMessage id='task.checklist.add' /></Typography>
        </Button>
      </Box>
    )
  }

  return (<>
    <Box display='flex' alignItems='center' justifyContent='space-between' paddingRight={2} paddingLeft={2.5}>
      <Typography fontWeight='bold'><FormattedMessage id='task.checklist' /></Typography>
      <Button startIcon={<AddIcon sx={{ color: 'uiElements.main' }} />} onClick={handleChecklistCreate}>
        <Typography sx={{ fontWeight: 'bold', color: 'text.primary', textTransform: 'capitalize' }}><FormattedMessage id='task.checklist.add' /></Typography>
      </Button>
    </Box>

    {/*  checklist section  */}

    {state.task.checklist.map((checklist) => (<Burger.Section key={checklist.id} width='95%'>
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
              backgroundColor: 'uiElements.light'
            }
          }}
        />
      </Box>

      <Stack spacing={1}>
        <Grid container key={checklist.id} id={checklist.id} direction='row' alignItems='center'
          sx={{
            color: 'text.primary',
            borderRadius: 1,
            width: '100%'
          }}>
        </Grid>

        {/* checklist items section  */}

        <Box display='flex' alignItems='center' justifyContent='flex-end' >
          <Button startIcon={<AddIcon sx={{ color: 'uiElements.main' }} />} onClick={() => handleChecklistItemCreate(checklist.id)}>
            <Typography sx={{ color: 'text.primary', textTransform: 'capitalize' }}>
              <FormattedMessage id='task.checklistItem.add' />
            </Typography>
          </Button>
        </Box>

        {checklist.items.map((item, index) => (<>

          <Grid container alignItems='center' key={item.id}>
            <Grid item md={2} lg={2}>
              <Typography fontWeight='bolder'><FormattedMessage id='task.checklist.item.title' /></Typography>
            </Grid>

            <Grid item md={10} lg={10} alignItems='center'>
              <Box display='flex' alignItems='center'>
                <Tooltip placement='top' arrow title={<FormattedMessage id='core.taskEdit.fields.checklistItem.tooltip.markCompleted' />}>
                  <Checkbox size='medium' sx={{ p: 0, mr: 1, '& .MuiSvgIcon-root': { color: 'uiElements.main' } }}
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
        </>

        ))}
      </Stack>

      {/* Add new checklist item  */}

      <Box display='flex' alignItems='center' justifyContent='flex-end' >
        <Button startIcon={<AddIcon sx={{ color: 'uiElements.main' }} />} onClick={() => handleChecklistItemCreate(checklist.id)}>
          <Typography sx={{ color: 'text.primary', textTransform: 'capitalize' }}>
            <FormattedMessage id='task.checklistItem.add' />
          </Typography>
        </Button>
      </Box>
    </Burger.Section >
    ))
    }
  </>)
}
export default TaskChecklist;