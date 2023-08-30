import React from 'react';
import { Box, Stack, Paper, Grid, Tabs, Tab, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { StyledTaskItem, SummaryTaskNotSelected, SummaryTaskSelected } from './TeamSpaceGroup';

import Client from '@taskclient';
import { TeamSpaceState, TeamSpaceTabState, init } from './types';


const TabPanel: React.FC<{ state: TeamSpaceTabState, children: React.ReactNode }> = ({ state, children }) => {

  return (
    <div hidden={state.disabled}>
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

  return (<>
    <Box position='fixed'>
      <Paper>
        <Tabs value={state.activeTab} onChange={handleActiveTab}>
          {state.tabs.map(tab => (<Tab key={tab.id} label={<Typography sx={{ fontWeight: 'bold', color: tab.color }}><FormattedMessage id={tab.label} /></Typography>} />))}
        </Tabs>
      </Paper>
    </Box>

    <Box sx={{ mt: 7 }} />

    <Grid container spacing={1}>
      <Grid item md={8} lg={8}>
        <Stack spacing={1}>
          {state.tabs.map(tab => (
            <TabPanel state={tab} key={tab.id}>
              {tab.group.records.map((task) => <StyledTaskItem key={task.id} task={task} onTask={() => handleActiveTask(task)} />)}
            </TabPanel>
          ))}
        </Stack>
      </Grid>

      <Grid item md={4} lg={4}>
        <Box display='flex' height='100%' position='fixed'>
          <Paper sx={{ p: 2 }}>
            <Stack direction='column' spacing={1}>
              {state.activeTask ? <SummaryTaskSelected task={state.activeTask} /> : <SummaryTaskNotSelected />}
            </Stack>
          </Paper>
        </Box>
      </Grid>
    </Grid>
  </>
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