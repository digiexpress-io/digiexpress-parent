import React from 'react';
import { Box, Stack, Grid, Tabs, Tab, Typography, AppBar, Toolbar, Divider, styled } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { StyledTaskItem, SummaryTaskNotSelected, SummaryTaskSelected } from './TeamSpaceGroup';
import Pagination from './Pagination';

import Client from '@taskclient';
import { TeamSpaceState, TeamSpaceTabState, init } from './types';


const StyledSummaryContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  height: '100%',
  position: 'fixed',
  paddingLeft: theme.spacing(2),
  paddingRight: theme.spacing(2),
  backgroundColor: theme.palette.background.paper
}));


const TabPanel: React.FC<{ state: TeamSpaceTabState, children: React.ReactNode }> = ({ state, children }) => {

  return (
    <div hidden={state.disabled}>
      {!state.disabled && (
        <Box sx={{ backgroundColor: 'mainContent.main' }}><Stack>{children}</Stack></Box>
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
    <AppBar position="sticky" color='inherit' sx={{ boxShadow: 'unset' }}>
      <Toolbar>
        <Tabs value={state.activeTab} onChange={handleActiveTab}>
          {state.tabs.map(tab => (<Tab key={tab.id}
            label={
              <Typography sx={{ fontWeight: 'bold', color: tab.color }}>
                <FormattedMessage id={tab.label} values={{ count: tab.count }} />
              </Typography>
            }
          />))}
        </Tabs>
        <Divider orientation='vertical' flexItem sx={{mx: 2}}/>
        <Pagination />
      </Toolbar>
    </AppBar>
    <Grid container>
      <Grid item md={8} lg={8}>
        <Stack spacing={1}>
          <Box sx={{ mt: 1 }} />
          {state.tabs.map(tab => (
            <TabPanel state={tab} key={tab.id}>
              {tab.group.records.map((task) => <StyledTaskItem key={task.id} task={task} onTask={() => handleActiveTask(task)} />)}
            </TabPanel>
          ))}
        </Stack>
      </Grid>

      <Grid item md={4} lg={4}>
        <StyledSummaryContainer>
          <Stack direction='column' spacing={1}>
            {state.activeTask ? <SummaryTaskSelected task={state.activeTask} /> : <SummaryTaskNotSelected />}
          </Stack>
        </StyledSummaryContainer>
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