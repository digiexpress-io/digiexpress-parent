import React from 'react';
import { Stack, Grid, lighten, Typography, AppBar, Toolbar, TablePagination, Button, SxProps } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import TaskItemActive from './TaskItemActive';
import TaskItem from './TaskItem';
import Client from '@taskclient';
import { MyWorkState, init } from './types';


const Tab: React.FC<{ children: React.ReactNode, active: boolean, color: string, onClick: () => void }> = ({ children, active, color, onClick }) => {
  const backgroundColor = active ? color : 'unset';
  const border = active ? undefined : '1px solid' + color;
  const sx: SxProps = {
    borderRadius: '8px 8px 0px 0px',
    boxShadow: "unset",
    backgroundColor,
    border,
    color: active ? 'mainContent.main' : color,
    borderBottom: 'unset',
    '&:hover': {
      backgroundColor: active ? color : lighten(color, 0.2),
      color: 'mainContent.main'
    },
    ml: 1
  };

  return (<Button variant="contained" sx={sx} onClick={onClick}>
    <Typography sx={{ fontWeight: 'bolder' }}>
      {children}
    </Typography>
  </Button>);
}


const initTable = (records: Client.TaskDescriptor[]) => new Client.TablePaginationImpl<Client.TaskDescriptor>({
  src: records,
  orderBy: 'dueDate',
  order: 'asc',
  sorted: true,
  rowsPerPage: 15,
});

const MyWork: React.FC<{ data: MyWorkState }> = ({ data }) => {
  const [state, setState] = React.useState<MyWorkState>(data);
  const [table, setTable] = React.useState(initTable([]));

  function handleActiveTab(newValue: number) {
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
      <AppBar color='inherit' position='sticky' sx={{ boxShadow: 'unset', px: 1, borderBottom: '1px solid' + state.tabs[state.activeTab].group.color }}>
        <Toolbar sx={{ alignItems: 'end', "&.MuiToolbar-root": { px: 'unset' } }}>

          {state.tabs.map(tab => (<Tab key={tab.id} active={state.activeTab === tab.id} color={tab.color} onClick={() => handleActiveTab(tab.id)} >
            <FormattedMessage id={tab.label} values={{ count: tab.count }} />
          </Tab>))}

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
      </AppBar >


      <Grid item md={8} lg={8}>
        <Stack sx={{ backgroundColor: 'mainContent.main' }}>
          {table.entries.map((task, index) => (<TaskItem 
            index={index}
            key={task.id}
            task={task}
            active={state.activeTask?.id === task.id}
            onTask={(task) => handleActiveTask(task)} />))}
        </Stack>
      </Grid>

      <Grid item md={4} lg={4}>
        <TaskItemActive task={state.activeTask} />
      </Grid>

    </Grid>
  );
}



const MyWorkLoader: React.FC = () => {
  const tasks = Client.useTasks();

  if (tasks.loading) {
    return <>...loading</>
  }

  return <MyWork data={init(tasks.state)} />;
}

export default MyWorkLoader;