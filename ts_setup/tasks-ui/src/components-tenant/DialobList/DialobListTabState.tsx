import React from 'react';
import { Stack, Grid, Typography, TablePagination, Alert } from '@mui/material';

import { FormattedMessage } from 'react-intl';
import Pagination from 'table';
import { initTable, initTabs, DialobListTabState, DialobListState } from './types';
import { StyledStackItem, StyledEditTaskButton, StyledStartTaskButton } from './DialobListStyles';
import { TenantEntryDescriptor } from 'descriptor-tenant';
import { NavigationSticky, NavigationButtonTaskList } from '../NavigationSticky';


const RowFiller: React.FC<{ value: Pagination.TablePagination<TenantEntryDescriptor> }> = ({ value }) => {

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

const DialobList: React.FC<{
  state: DialobListTabState[]
  children: {
    DialobItem: React.ElementType<{ entry: TenantEntryDescriptor }>;
    DialobItemActive: React.ElementType<{ entry: TenantEntryDescriptor | undefined }>;
  }
}> = ({ state: initTabsState, children }) => {

  const [state, setState] = React.useState<DialobListState>(initTabs([]));
  const [table, setTable] = React.useState(initTable([]));
  const [createOpen, setCreateOpen] = React.useState(false);

  function handleTaskCreate() {
    setCreateOpen(prev => !prev)
  }

  function handleActiveTab(newValue: number) {
    setState(prev => prev.withActiveTab(newValue));
  }

  function handleActiveTask(task: TenantEntryDescriptor | undefined) {
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

  const { DialobItem, DialobItemActive } = children;


  return (<>
    <Grid container>

      <NavigationSticky>
        {state.tabs.map(tab => (
          <NavigationButtonTaskList
            id={tab.label}
            values={{ count: tab.count }}
            key={tab.id}
            active={state.activeTab === tab.id}
            color={tab.color}
            onClick={() => handleActiveTab(tab.id)} />
        ))
        }
        <NavigationButtonTaskList
          id='core.taskCreate.newTask'
          onClick={handleTaskCreate}
          values={undefined}
          active={createOpen}
          color={'rgb(80, 72, 229)'}
        />
      </NavigationSticky>

      <Grid item md={8} lg={8}>
        <Stack sx={{ backgroundColor: 'mainContent.main' }}>
          {table.entries.map((task, index) => (
            <StyledStackItem key={task.source.id} index={index} active={state.activeDialob?.source.id === task.source.id} onClick={() => handleActiveTask(task)}>
              <DialobItem key={task.source.id} entry={task} />
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
        <DialobItemActive entry={state.activeDialob} />
      </Grid>
    </Grid>
  </>
  );
}

export type { DialobListTabState };
export { DialobList, StyledEditTaskButton, StyledStartTaskButton };



