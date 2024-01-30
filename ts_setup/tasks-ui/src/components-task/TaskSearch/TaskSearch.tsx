import React from 'react';
import { Box, Stack } from '@mui/material';

import { FilterByString, NavigationSticky } from 'components-generic';

import { FilterStatus } from './TableFilters/FilterStatus';
import { FilterAssignees } from './TableFilters/FilterAssignees';
import { FilterRoles } from './TableFilters/FilterRoles';
import { FilterPriority } from './TableFilters/FilterPriority';
import { FilterColumns } from './TableFilters/FilterColumns';
import { GroupBySelect } from './TableFilters/GroupBy';
import { TableForGroupBy } from './Table'; 

import { 
  TaskSearchProvider, TaskPopperContext, TaskGroupingProvider, TaskPrefsProvider, 
  useSearch, useGrouping, useTaskPrefs,  ColumnNameOptions, GroupByTypes,
  TaskReloadProvider
} from './TableContext';
import { TaskDescriptor } from 'descriptor-task';



const Navigation: React.FC<{}> = () => {
  const ctx = useSearch();
  const { pref, withVisibleFields } = useTaskPrefs();
  const selectedCols = pref.visibility.filter(v => v.enabled).map(v => v.dataId as (keyof TaskDescriptor));

  return (
    <NavigationSticky>
      <FilterByString onChange={({ target }) => ctx.withSearchString(target.value)} />
      <Stack direction='row' spacing={1}>
        <GroupBySelect value={ctx.groupBy} onChange={(value) => ctx.withGrouBy(value)} />
        <FilterStatus value={ctx.state.filterBy} onChange={(value) => ctx.withFilterByStatus(value)} />
        <FilterPriority value={ctx.state.filterBy} onChange={(value) => ctx.withFilterByPriority(value)} />
        <FilterAssignees value={ctx.state.filterBy} onChange={(value) => ctx.withFilterByOwner(value)} />
        <FilterRoles value={ctx.state.filterBy} onChange={(value) => ctx.withFilterByRoles(value)} />
        <FilterColumns types={ColumnNameOptions} value={selectedCols} onChange={(value) => withVisibleFields(value)} />
      </Stack>
    </NavigationSticky>
  );
}

const TaskTableGroup: React.FC = () => {
  const { collection } = useGrouping();

  return (<>{collection.groups.map((group, index) => (
    <React.Fragment key={group.id}>
      {index > 0 ? <Box sx={{ p: 2 }} /> : null}

      <Box sx={{ width: '100%' }}>
        <TableForGroupBy groupId={group.id} groupByType={collection.classifierName as GroupByTypes} />
      </Box>
    </React.Fragment>
  ))}</>);
}


export const TaskSearch: React.FC<{}> = () => {

  return (
    <TaskPopperContext>
      <TaskSearchProvider>
        <TaskGroupingProvider>
          <TaskPrefsProvider>
            <TaskReloadProvider />
            <Navigation />
            <Box mt={1} />
            <TaskTableGroup />
          </TaskPrefsProvider>
        </TaskGroupingProvider>
      </TaskSearchProvider>
    </TaskPopperContext>
  );
}