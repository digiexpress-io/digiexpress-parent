import React from 'react';
import { Box, Stack, Grid, Tabs, Tab, Typography, AppBar, Toolbar, Divider } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import TaskItemActive from './TaskItemActive';
import TaskItem from './TaskItem';
import Pagination from './Pagination';

import Client from '@taskclient';
import { TeamSpaceState, TeamSpaceTabState, init } from './types';


const TabPanel: React.FC<{ state: TeamSpaceTabState, children: React.ReactNode }> = ({ state, children }) => {
  return (
    <div hidden={state.disabled}>
      {!state.disabled && (
        <Box sx={{ 
          backgroundColor: 'mainContent.main'
        }}>
          <Stack>{children}</Stack>
        </Box>
      )}
    </div>);
}


const TeamSpace: React.FC<{ data: TeamSpaceState }> = ({ data }) => {
  const [state, setState] = React.useState<TeamSpaceState>(data);
  function handleActiveTab(_event: React.SyntheticEvent, newValue: number) {
    setState(prev => prev.withActiveTab(newValue));
  }

  function handleActiveTask(task: Client.TaskDescriptor | undefined) {
    setState(prev => prev.withActiveTask(task));
  }

  return (<>
    <AppBar position="sticky" color='inherit' sx={{ boxShadow: 'unset' }}>
      <Toolbar sx={{alignItems: 'end', "&.MuiToolbar-root": { px: 'unset'}}}>
        <Tabs value={state.activeTab} onChange={handleActiveTab} TabIndicatorProps={{ sx: { display: 'none' } }}>
          {state.tabs.map(tab => (<Tab key={tab.id}
            sx={{
              mx: 1, borderRadius: '8px 8px 0px 0px',
              backgroundColor: state.activeTab === tab.id ? tab.color : undefined,
              border: state.activeTab === tab.id ? undefined : '1px solid' + tab.color
            }}
            label={
              <Typography sx={{ fontWeight: 'bolder', color: state.activeTab === tab.id ? 'mainContent.main' : 'text.primary' }}>
                <FormattedMessage id={tab.label} values={{ count: tab.count }} />
              </Typography>
            }
          />))}
        </Tabs>
        <Divider orientation='vertical' flexItem sx={{ mx: 2 }} />
        
        <Pagination />
      </Toolbar>
    </AppBar>
    <Grid container spacing={1}>
      <Grid item md={8} lg={8}>
        <Stack spacing={1}>
          <Box sx={{ mt: 1 }} />
          {state.tabs.map(tab => (
            <TabPanel state={tab} key={tab.id}>
              {tab.group.records.map((task) => <TaskItem
                key={task.id}
                task={task}
                active={state.activeTask?.id === task.id}
                onTask={(task) => handleActiveTask(task)} />)}
            </TabPanel>
          ))}
        </Stack>
      </Grid>

      <Grid item md={4} lg={4}>
        <TaskItemActive task={state.activeTask} />
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