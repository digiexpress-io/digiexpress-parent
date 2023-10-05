import React from 'react';
import { Box, Stack, Paper, Grid, Typography } from '@mui/material';

import TaskGroup from './MyWorkGroup';
import { FormattedMessage } from 'react-intl';
import MyRecentActivity from './MyRecentActivity';

import Client from '@taskclient';

const MyWork: React.FC<{ groups: Client.Group[] }> = ({ groups }) => {

  return (<>
    <Box width="100%">
      <Grid container spacing={1}>
        <Grid item md={8} lg={8}>
          <Box display="flex" justifyContent="center">
            <Paper sx={{display:'flex', position:'fixed', p: 3}} elevation={2}>
              *************************  TODO FILTERS  **************************
            </Paper>
          </Box>
          <Paper sx={{pt: 7}}>
            <Stack sx={{p:3}}>
              {groups.map( group => <TaskGroup key={group.id} group={group} /> )}
            </Stack>
          </Paper>
        </Grid>

        <Grid item md={4} lg={4}>
          <Box sx={{display:'flex', position:'fixed', pr: 3, height: "100%"}}>
            <Paper sx={{p: 3}}>
              <Typography variant='h4' fontWeight='bold'><FormattedMessage id={"core.myWork.recentActivities.title"} /></Typography>
              <MyRecentActivity />
            </Paper>
          </Box>
        </Grid>
      </Grid>
    </Box>
  </>
  );
}

const MyWorkLoader: React.FC = () => {
  const tasks = Client.useTasks();  
  const { loading, state } = tasks;  
  const groups = React.useMemo(() => state.withGroupBy('assignee').groups, [state]);      
  
  if (loading) {    
    return <>...loading</>  
  }  

  return <MyWork groups={groups} />;
}

export default MyWorkLoader;