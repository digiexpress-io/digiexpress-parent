import React from 'react';
import { Box, Typography, IconButton, TextField, Checkbox, Stack, Grid, Divider, Tooltip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import { FormattedMessage } from 'react-intl';
import Context from 'context';
import Client from 'taskclient';
import TaskAssignees from '../TaskAssignees';
import DueDate from '../TaskDueDate';
import Section from '../Section';



const TaskChecklist: React.FC<{ onChange: (commands: Client.TaskUpdateCommand<any>[]) => Promise<void> }> = ({ onChange }) => {
  const { state } = Context.useTaskEdit();

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

  if (!state.task.checklist.length) {
    return (
      <Box display='flex' alignItems='center'>
        <Typography textTransform='capitalize' sx={{ color: 'text.primary' }}><FormattedMessage id='task.checklist.add' /></Typography>
        <Box flexGrow={1} />
        <IconButton><AddIcon sx={{ color: 'uiElements.main' }} /></IconButton>
      </Box>)
  }

  return (<>
    {state.task.checklist.map((checklist) => (<Section key={checklist.id} width='95%'>
      <Box>
        <TextField defaultValue={checklist.title} onChange={() => { }}
          fullWidth
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
              <Typography fontWeight='bolder'><FormattedMessage id='task.checklist.item.title' /></Typography>
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

          <Grid container alignItems='center' >
            <Grid item md={2} lg={2}>
              <Typography fontWeight='bolder'><FormattedMessage id='task.checklist.item.dueDate' /></Typography>
            </Grid>
            <Grid item md={10} lg={10}>
              <DueDate task={{ dueDate: item.dueDate ? new Date(item.dueDate) : undefined }} onChange={(dueDate) => handleDueDateChange(dueDate, checklist.id, item.id)} />
            </Grid>
          </Grid>
          <Grid container>
            <Grid item md={2} lg={2} alignSelf='center'>
              <Typography fontWeight='bolder'>Assignees</Typography>
            </Grid>
            <Grid item md={10} lg={10}><TaskAssignees onChange={(users) => handleAssigneesChange(users, checklist.id, item.id)} task={{ assignees: item.assigneeIds }} /></Grid>
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