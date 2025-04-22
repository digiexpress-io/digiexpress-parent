import React from 'react';
import { Typography } from '@mui/material';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

import { Column } from '@tanstack/react-table';
import { EveliTableColumnOptions } from './EveliTableColumnOptions';
import { TaskApi } from '@/api-task';



type EveliTableHeaderProps = {
  children: React.ReactNode;
  filterComponent?: React.ReactNode | undefined;
  column: Column<TaskApi.Task, unknown>;
  sortDirection?: false | 'asc' | 'desc';
  onColumnFilter: () => void;
  onResetColVisibility: () => void;
  isFilterable: boolean;
  isSortable: boolean;
}

//TODO clean up of isFilterable / isSortable
export const EveliTableHeaderCell: React.FC<EveliTableHeaderProps> = (props) => {

  if (props.isSortable) {
    return (
      <div className='headerCell' style={{ width: props.column.getSize() }}>
        <Typography>{props.children}</Typography>
        <div style={{ marginLeft: 4, display: 'flex' }}>
          {props.sortDirection === 'asc' && <ArrowUpwardIcon fontSize="small" />}
          {props.sortDirection === 'desc' && <ArrowDownwardIcon fontSize="small" />}
        </div>
        <div style={{ flexGrow: 1 }} />

        {props.isFilterable ? props.filterComponent : undefined}

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
      {props.isFilterable ? props.filterComponent : undefined}
    </div>

  )
}
