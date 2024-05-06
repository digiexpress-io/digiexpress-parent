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
  TaskReloadProvider, PrefConfigFields
} from './TableContext';
import { TaskDescriptor } from 'descriptor-task';



const Navigation: React.FC<{}> = () => {
  const ctx = useSearch();
  const { pref, withVisibleFields, withConfig } = useTaskPrefs();
  const selectedCols = pref.visibility.filter(v => v.enabled).map(v => v.dataId as (keyof TaskDescriptor));
  
  return (
    <NavigationSticky>
      <FilterByString defaultValue={ctx.state.searchString ?? ''} onChange={({ target }) => {
        ctx.withSearchString(target.value);
        withConfig({dataId: PrefConfigFields.searchBy, value: target.value});
      }} />
      
      <GroupBySelect value={ctx.groupBy} onChange={(value) => {
        ctx.withGrouBy(value)
        withConfig({dataId: PrefConfigFields.groupBy, value});
      }} />
      <FilterStatus value={ctx.state.filterBy} onChange={(value) => ctx.withFilterByStatus(value)} />
      <FilterPriority value={ctx.state.filterBy} onChange={(value) => ctx.withFilterByPriority(value)} />
      <FilterAssignees value={ctx.state.filterBy} onChange={(value) => ctx.withFilterByOwner(value)} />
      <FilterRoles value={ctx.state.filterBy} onChange={(value) => ctx.withFilterByRoles(value)} />
      <FilterColumns types={ColumnNameOptions} value={selectedCols} onChange={(value) => withVisibleFields(value)} />

    </NavigationSticky>
  );
}

const OneTable: React.FC<{ index: number, groupId: string, classifierName: GroupByTypes }> = React.memo((props) => {
  return (<React.Fragment key={props.groupId}>
    {props.index > 0 ? <Box sx={{ p: 2 }} /> : null}

    <Box sx={{ width: '100%' }}>
      <TableForGroupBy groupId={props.groupId} groupByType={props.classifierName} />
    </Box>
  </React.Fragment>)
})

const ManyTables: React.FC = () => {
  const { collection } = useGrouping();

  return (<>{collection.groups.map((group, index) => <OneTable key={group.id}
    index={index} 
    groupId={group.id} 
    classifierName={collection.classifierName as GroupByTypes}
  />)}</>);
}


export const TaskSearch: React.FC<{}> = () => {

  return (
    <TaskPopperContext>
      <TaskPrefsProvider>
        <TaskSearchProvider>
          <TaskGroupingProvider>
            
            <TaskReloadProvider />
            <Navigation />

            <Box mt={1} />
            <ManyTables />
          
          </TaskGroupingProvider>
        </TaskSearchProvider>
      </TaskPrefsProvider>
    </TaskPopperContext>
  );
}