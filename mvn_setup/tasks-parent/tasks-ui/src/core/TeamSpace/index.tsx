import React from 'react';
import { Box, Stack, Paper, Grid, Tabs, Tab, Typography, AppBar } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { StyledTaskItem, SummaryTaskNotSelected, SummaryTaskSelected } from './TeamSpaceGroup';
import Client from '@taskclient';


import { TeamSpaceState, TeamSpaceTabState, init } from './types';


const TabPanel: React.FC<{ state: TeamSpaceTabState, children: React.ReactNode }> = ({ state, children }) => {

  return (<div hidden={state.disabled} key={state.id}>

    {!state.disabled && (
      <Paper sx={{ p: 2 }}><Stack>{children}</Stack></Paper>
    )}

  </div>);
}


const TeamSpace: React.FC<{ data: TeamSpaceState }> = ({ data }) => {
  const [state, setState] = React.useState<TeamSpaceState>(data);

  function handleActiveTab(_event: React.SyntheticEvent, newValue: number) {
    setState(prev => prev.withActiveTab(newValue));
  }

  function handleActiveTask(task: Client.TaskDescriptor) {
    setState(prev => prev.withActiveTask(task));
  }

  return (
    <Grid container spacing={1}>
      <Grid item md={12} lg={12}>
        <Box display='flex' position='fixed'>
          <Paper>
            <Tabs value={state.activeTab} onChange={handleActiveTab}>
              {state.tabs.map(tab => (<Tab key={tab.id} label={<Typography sx={{ fontWeight: 'bold', color: tab.color }}><FormattedMessage id={tab.label} /></Typography>} />))}
            </Tabs>
          </Paper>
        </Box>
      </Grid>

      <Box sx={{ mb: 7 }} />

      <Grid item md={9} lg={9}>
        <Stack spacing={1}>
          {state.tabs.map(tab => (
            <TabPanel state={tab}>
              {tab.group.records.map((task) => <StyledTaskItem key={task.id} task={task} onTask={() => handleActiveTask(task)} />)}
            </TabPanel>
          ))}
        </Stack>
      </Grid>

      <Grid item md={3} lg={3}>
        <Box display='flex' height='100%' position='fixed'>
          <Paper sx={{ p: 2 }}>
            <Stack direction='column' spacing={1}>
              {state.activeTask ? <SummaryTaskSelected task={state.activeTask} /> : <SummaryTaskNotSelected />}
            </Stack>
          </Paper>
        </Box>
      </Grid>
    </Grid>
  );
}



const TeamSpaceLoader: React.FC = () => {
  const tasks = Client.useTasks();

  if (tasks.loading) {
    return <>...loading</>
  }

  return <TeamSpace data={init(tasks.state)} />;
}

export default TeamSpaceLoader;