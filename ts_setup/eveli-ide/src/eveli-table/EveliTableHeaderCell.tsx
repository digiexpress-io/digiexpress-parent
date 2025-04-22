import React from 'react';
import { Typography } from '@mui/material';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

import { Column } from '@tanstack/react-table';
import { EveliTableColumnFilter } from './EveliTableColumnFilter';
import { EveliTableColumnOptions } from './EveliTableColumnOptions';



type EveliTableHeaderProps<T> = {
  children: React.ReactNode;
  column: Column<T, unknown>;
  sortDirection?: false | 'asc' | 'desc';
}

interface ExtraProps {
  onColumnFilter: () => void;
}


// for testing
export const EveliTableHeaderCell = <T,>(props: EveliTableHeaderProps<T> & ExtraProps) => {
  const sortDirection = props.column.getIsSorted();
  const isSortable = props.column.getCanSort();

  if (!isSortable) {
    return (
      <div className='headerCell' style={{ width: props.column.getSize() }}>
        <Typography>{props.children}</Typography>
        <div style={{ flexGrow: 1 }} />
        <EveliTableColumnFilter filterItems={['filter 1']} />
      </div>

    )
  }
  return (
    <div className='headerCell' style={{ width: props.column.getSize() }}>
      <Typography>{props.children}</Typography>
      <div style={{ marginLeft: 4, display: 'flex' }}>
        {sortDirection === 'asc' && <ArrowUpwardIcon fontSize="small" />}
        {sortDirection === 'desc' && <ArrowDownwardIcon fontSize="small" />}
      </div>
      <div style={{ flexGrow: 1 }} />
      <EveliTableColumnFilter filterItems={['filter 1']} />
      <EveliTableColumnOptions
        onChooseCols={props.onColumnFilter}
        onSortAsc={() => props.column.toggleSorting(false)}
        onSortDesc={() => props.column.toggleSorting(true)}
        onClearSorting={() => props.column.clearSorting()}
      />
    </div>

  )
}
