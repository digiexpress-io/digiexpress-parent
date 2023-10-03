import React from 'react';
import { Stack, Grid, Tabs, Tab, Typography, AppBar, Toolbar, Divider, TablePagination } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import TaskItemActive from './TaskItemActive';
import TaskItem from './TaskItem';
import Client from '@taskclient';
import { TeamSpaceState, init } from './types';


const initTable = (records: Client.TaskDescriptor[]) => new Client.TablePaginationImpl<Client.TaskDescriptor>({
  src: records,
  orderBy: 'dueDate',
  order: 'asc',
  sorted: true,
  rowsPerPage: 15,
});

const TeamSpace: React.FC<{ data: TeamSpaceState }> = ({ data }) => {
  const [state, setState] = React.useState<TeamSpaceState>(data);
  const [table, setTable] = React.useState(initTable([]));

  function handleActiveTab(_event: React.SyntheticEvent, newValue: number) {
    setState(prev => prev.withActiveTab(newValue));
  }

  function handleActiveTask(task: Client.TaskDescriptor | undefined) {
    setState(prev => prev.withActiveTask(task));
  }

  function handleOnPageChange(_garbageEvent: any, newPage: number) {
    setTable((state) => state.withPage(newPage));
  }

  function handleOnRowsPerPageChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTable((state) => state.withRowsPerPage(parseInt(event.target.value, 10)))
  }


  React.useEffect(() => {
    const { activeTab } = state;
    const { records } = state.tabs[activeTab].group;
    setTable((src) => src.withSrc(records).withPage(0));
  }, [state, setTable])


  return (
    <Grid container spacing={1}>
      <AppBar color='inherit' position='sticky' sx={{ boxShadow: 'unset', px: 1 }}>
        <Toolbar sx={{ alignItems: 'end', "&.MuiToolbar-root": { px: 'unset' } }}>
          <Tabs value={state.activeTab} onChange={handleActiveTab} TabIndicatorProps={{ sx: { display: 'none' } }}>
            {state.tabs.map(tab => (<Tab key={tab.id}
              sx={{
                mx: 1, borderRadius: '8px 8px 0px 0px',
                backgroundColor: state.activeTab === tab.id ? tab.color : undefined,
                border: state.activeTab === tab.id ? undefined : '1px solid' + tab.color,
                borderBottom: 'unset',
                boxShadow: 4
              }}
              label={
                <Typography sx={{ fontWeight: 'bolder', color: state.activeTab === tab.id ? 'mainContent.main' : tab.color }}>
                  <FormattedMessage id={tab.label} values={{ count: tab.count }} />
                </Typography>
              }
            />))}
          </Tabs>

          <TablePagination
            rowsPerPageOptions={table.rowsPerPageOptions}
            component="div"
            count={table.src.length}
            rowsPerPage={table.rowsPerPage}
            page={table.page}
            onPageChange={handleOnPageChange}
            onRowsPerPageChange={handleOnRowsPerPageChange}
          />
        </Toolbar>
        <Divider />
      </AppBar >
      <Grid item md={8} lg={8}>
        <Stack sx={{ backgroundColor: 'mainContent.main' }}>
          {table.entries.map((task) => <TaskItem
            key={task.id}
            task={task}
            active={state.activeTask?.id === task.id}
            onTask={(task) => handleActiveTask(task)} />)}
        </Stack>
      </Grid>

      <Grid item md={4} lg={4}>
        <TaskItemActive task={state.activeTask} />
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