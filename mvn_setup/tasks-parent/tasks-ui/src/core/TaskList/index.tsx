import React from 'react';
import { Stack, Grid, Typography, TablePagination, Alert, IconButton, Box } from '@mui/material';
import AddBoxIcon from '@mui/icons-material/AddBox';

import { FormattedMessage } from 'react-intl';
import Pagination from 'table';
import { TaskListState, initTable, initTabs, TaskListTabState } from './types';
import { StyledStackItem, StyledTaskListTab, StyledEditTaskButton, StyledStartTaskButton, StyledAppBar } from './TaskListStyles';
import { TaskDescriptor } from 'taskdescriptor';
import TaskCreateDialog from '../TaskCreate';

const RowFiller: React.FC<{ value: Pagination.TablePagination<TaskDescriptor> }> = ({ value }) => {

  if (value.entries.length === 0) {
    return (<Alert sx={{ m: 2 }} severity='info'>
      <Typography><FormattedMessage id='core.myWork.alert.entries.none' /></Typography>
    </Alert>);
  }
  const result: React.ReactNode[] = []
  for (let index = 0; index < value.emptyRows; index++) {
    result.push(<StyledStackItem active={false} key={index} index={value.entries.length + index} onClick={() => { }} children="" />)
  }

  return <>{result}</>
}

const TaskList: React.FC<{
  state: TaskListTabState[]
  children: {
    TaskItem: React.ElementType<{ task: TaskDescriptor }>;
    TaskItemActive: React.ElementType<{ task: TaskDescriptor | undefined }>;
  }
}> = ({ state: initTabsState, children }) => {

  const [state, setState] = React.useState<TaskListState>(initTabs([]));
  const [table, setTable] = React.useState(initTable([]));
  const [createOpen, setCreateOpen] = React.useState(false);

  function handleTaskCreate() {
    setCreateOpen(prev => !prev)
  }

  function handleActiveTab(newValue: number) {
    setState(prev => prev.withActiveTab(newValue));
  }

  function handleActiveTask(task: TaskDescriptor | undefined) {
    setState(prev => prev.withActiveTask(task));
  }

  function handleOnPageChange(_garbageEvent: any, newPage: number) {
    setTable((state) => state.withPage(newPage));
  }

  function handleOnRowsPerPageChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTable((state) => state.withRowsPerPage(parseInt(event.target.value, 10)))
  }

  React.useEffect(() => {
    if (state.tabs.length === 0) {
      return;
    }
    const { activeTab } = state;
    const { records } = state.tabs[activeTab].group;
    setTable((src) => src.withSrc(records).withPage(0));
  }, [state, setTable])

  React.useEffect(() => {
    setState(prev => prev.withTabs(initTabsState))
  }, [initTabsState]);

  if (state.tabs.length === 0) {
    return null;
  }

  const { TaskItem, TaskItemActive } = children;

  return (<>
    <TaskCreateDialog open={createOpen} onClose={handleTaskCreate} />
    <Grid container>
      <Grid item md={8} lg={8}>
        <StyledAppBar color={state.tabs[state.activeTab].group.color}>
          {state.tabs.map(tab => (
            <StyledTaskListTab key={tab.id} active={state.activeTab === tab.id} color={tab.color} onClick={() => handleActiveTab(tab.id)} >
              <FormattedMessage id={tab.label} values={{ count: tab.count }} />
            </StyledTaskListTab>))
          }
          <Box flexGrow={1} />
          <IconButton sx={{ color: 'uiElements.main' }} onClick={handleTaskCreate}><AddBoxIcon /></IconButton>
        </StyledAppBar >


        <Stack sx={{ backgroundColor: 'mainContent.main' }}>
          {table.entries.map((task, index) => (
            <StyledStackItem key={task.id} index={index} active={state.activeTask?.id === task.id} onClick={() => handleActiveTask(task)}>
              <TaskItem key={task.id} task={task} />
            </StyledStackItem>)
          )}
          <RowFiller value={table} />
        </Stack>

        <TablePagination
          rowsPerPageOptions={table.rowsPerPageOptions}
          component="div"
          count={table.src.length}
          rowsPerPage={table.rowsPerPage}
          page={table.page}
          onPageChange={handleOnPageChange}
          onRowsPerPageChange={handleOnRowsPerPageChange}
        />
      </Grid>

      <Grid item md={4} lg={4}>
        <TaskItemActive task={state.activeTask} />
      </Grid>
    </Grid>
  </>
  );
}

export type { TaskListTabState };
export { TaskList, StyledEditTaskButton, StyledStartTaskButton, StyledTaskListTab, StyledAppBar };



