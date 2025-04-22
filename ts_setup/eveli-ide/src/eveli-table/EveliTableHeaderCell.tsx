import React from 'react';
import { collapseClasses, Typography } from '@mui/material';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

import { Column } from '@tanstack/react-table';
import { EveliTableColumnFilter } from './EveliTableColumnFilter';
import { EveliTableColumnOptions } from './EveliTableColumnOptions';
import { TaskApi } from '@/api-task';



type EveliTableHeaderProps = {
  children: React.ReactNode;
  column: Column<TaskApi.Task, unknown>;
  sortDirection?: false | 'asc' | 'desc';
  onColumnFilter: () => void;
  onResetColVisibility: () => void;
}

//TODO clean up of isFilterable / isSortable
export const EveliTableHeaderCell: React.FC<EveliTableHeaderProps> = (props: EveliTableHeaderProps): React.ReactElement => {
  const sortDirection = props.column.getIsSorted();
  const isSortable = props.column.getCanSort();
  const isFilterable = props.column.getCanFilter();

  if (isSortable) {
    return (
      <div className='headerCell' style={{ width: props.column.getSize() }}>
        <Typography>{props.children}</Typography>
        <div style={{ marginLeft: 4, display: 'flex' }}>
          {sortDirection === 'asc' && <ArrowUpwardIcon fontSize="small" />}
          {sortDirection === 'desc' && <ArrowDownwardIcon fontSize="small" />}
        </div>
        <div style={{ flexGrow: 1 }} />

        {isFilterable ? <EveliTableColumnFilter filterItems={['filter 1']} column={props.column} /> : undefined}

        <EveliTableColumnOptions
          onChooseCols={props.onColumnFilter}
          onSortAsc={() => props.column.toggleSorting(false)}
          onSortDesc={() => props.column.toggleSorting(true)}
          onClearSorting={() => props.column.clearSorting()}
          onClearColVisibility={() => props.onResetColVisibility()}
        />
      </div>
    )
  }
  return (
    <div className='headerCell' style={{ width: props.column.getSize() }}>
      <Typography>{props.children}</Typography>
      <div style={{ flexGrow: 1 }} />
      {isFilterable ? <EveliTableColumnFilter filterItems={['filter 1']} column={props.column} /> : undefined}
    </div>

  )
}
