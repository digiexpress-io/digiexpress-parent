import React from 'react';

import { FilterByString, NavigationSticky } from 'components-generic';

import { FilterStatus } from './TableFilters/FilterStatus';
import { FilterAssignees } from './TableFilters/FilterAssignees';
import { FilterRoles } from './TableFilters/FilterRoles';
import { FilterPriority } from './TableFilters/FilterPriority';
import { FilterColumns } from './TableFilters/FilterColumns';
import { GroupBySelect } from './TableFilters/GroupBy';


import { useSearch, useTaskPrefs,  ColumnNameOptions, PrefConfigFields } from './TableContext';
import { TaskDescriptor } from 'descriptor-task';



export const TaskSearchNavigation: React.FC<{}> = () => {
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
