import React from 'react';
import { Box, Typography, IconButton, TextField, Checkbox, Stack, Grid, Divider, Tooltip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import { FormattedMessage } from 'react-intl';
import Context from 'context';
import Client from 'taskclient';
import TimestampFormatter from '../TimestampFormatter';
import TaskAssignees from '../TaskAssignees';
import Section from '../Section';



const TaskChecklist: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();
  const backend = Context.useBackend();

  async function handleCheclistItemAssigneeChange(assigneeIds: Client.UserId[]) {
    const command: Client.ChangeChecklistItemAssignees = {
      assigneeIds,
      checklistItemId: state.task.checklist[0].items[0].id,
      checklistId: state.task.checklist[0].id,
      commandType: 'ChangeChecklistItemAssignees',
      taskId: state.task.id
    };
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }


  if (!state.task.checklist.length) {
    return (
      <Box display='flex' alignItems='center'>
        <Typography textTransform='capitalize' sx={{ color: 'text.primary' }}><FormattedMessage id='task.checklist.add' /></Typography>
        <Box flexGrow={1} />
        <IconButton><AddIcon sx={{ color: 'uiElements.main' }} /></IconButton>
      </Box>)
  }

  return (<>
    {state.task.checklist.map((checklist) => (<Section width='95%'>
      <Box>
        <TextField defaultValue={checklist.title} fullWidth
          InputProps={{
            endAdornment: (<IconButton><MoreHorizOutlinedIcon sx={{ color: 'uiElements.main' }} /></IconButton>),
            sx: {
              height: '2rem',
              fontWeight: 'bold',
              backgroundColor: 'uiElements.light'
            }
          }}
        />
      </Box>

      <Stack spacing={1}>
        <Grid container key={checklist.id} id={checklist.id} direction='row' alignItems='center'
          sx={{
            pr: 0,
            color: 'text.primary',
            borderRadius: 1,
            width: '100%'
          }}>

        </Grid>
        {checklist.items.map((item) => (<>
          <Grid container alignItems='center' key={item.id}>
            <Grid item md={2} lg={2}>
              <Typography fontWeight='bolder'>To-do item</Typography>
            </Grid>

            <Grid item md={10} lg={10}>
              <Box display='flex' alignItems='center'>
                <TextField value={item.title} fullWidth InputProps={{ sx: { height: '2.5rem' } }} />
                <Tooltip placement='top' arrow title={<FormattedMessage id='core.taskEdit.fields.checklistItem.tooltip.markCompleted' />}>
                  <Checkbox size='small' sx={{ '& .MuiSvgIcon-root': { color: 'uiElements.main' } }} />
                </Tooltip>
              </Box>
            </Grid>
          </Grid>

          <Grid container>
            <Grid item md={2} lg={2}>
              <Typography fontWeight='bolder'>Due date</Typography>
            </Grid>
            <Grid item md={10} lg={10}>
              <Typography><TimestampFormatter type='date' value={item.dueDate ? new Date(item.dueDate) : undefined} /></Typography>
            </Grid>
          </Grid>
          <Grid container>
            <Grid item md={2} lg={2} alignSelf='center'>
              <Typography fontWeight='bolder'>Assignees</Typography>
            </Grid>
            <Grid item md={10} lg={10}><TaskAssignees onChange={handleCheclistItemAssigneeChange} task={state.task} /></Grid>
          </Grid>

          <Divider />
        </>
        ))}
      </Stack>
    </Section >
    ))
    }
  </>)
}
export default TaskChecklist;