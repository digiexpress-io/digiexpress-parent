import React from 'react';
import { Box,  Stack, Paper, Grid, } from '@mui/material';

import Client from '@taskclient';
import {StyledTaskItem, Header, SummaryTaskNotSelected, SummaryTaskSelected} from './TeamSpaceGroup';



const TeamSpace: React.FC = () => {
  const tasks = Client.useTasks();
  const { loading, state } = tasks;
  const [task, setTask] = React.useState<Client.TaskDescriptor>();
  const groups = React.useMemo(() => state.withGroupBy("team").groups, [state]);


  return (
    <Grid container spacing={1}>
      <Grid item md={9} lg={9}>
        <Stack spacing={1}>
          {groups.map((group) => (<Paper sx={{ p: 2 }} key={group.id}>
            <Header group={group} />
            <Stack>{group.records.map((task) => <StyledTaskItem key={task.id} task={task} onTask={setTask} />)}</Stack>
          </Paper>))}
        </Stack>
      </Grid>


      <Grid item md={3} lg={3}>
        <Box display='flex' height='100%' position='fixed'>
          <Paper sx={{ p: 2 }}>
            <Stack direction='column' spacing={1}>
              {task ? <SummaryTaskSelected task={task} /> : <SummaryTaskNotSelected />}
            </Stack>
          </Paper>
        </Box>
      </Grid>
    </Grid>
  );
}

export default TeamSpace;