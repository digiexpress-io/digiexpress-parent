import React from 'react';
import { Typography } from '@mui/material';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

import { Column } from '@tanstack/react-table';

import { EveliTableColumnFilterDialog } from './EveliTableColumnFilterDialog';
import { EveliTableColumnFilter } from './EveliTableColumnFilter';
import { EveliTableColumnOptions } from './EveliTableColumnOptions';



type EveliTableHeaderProps<T> = {
  children: React.ReactNode;
  column: Column<T, unknown>;
  sortDirection?: false | 'asc' | 'desc';
}


// for testing
export const EveliTableHeaderCell = <T,>({ children, column }: EveliTableHeaderProps<T>) => {
  const [open, setOpen] = React.useState(false);
  const sortDirection = column.getIsSorted();
  const isSortable = column.getCanSort();

  if (!isSortable) {
    return (
      <>
        <EveliTableColumnFilterDialog open={open} onClose={() => setOpen(false)} />
        <div className='headerCell' style={{ width: column.getSize() }}>
          <Typography>{children}</Typography>
          <div style={{ flexGrow: 1 }} />
          <EveliTableColumnFilter filterItems={['filter 1']} />
        </div>
      </>
    )
  }
  return (
    <>
      <EveliTableColumnFilterDialog open={open} onClose={() => setOpen(false)} />

      <div className='headerCell' style={{ width: column.getSize() }}>
        <Typography>{children}</Typography>
        <div style={{ marginLeft: 4, display: 'flex' }}>
          {sortDirection === 'asc' && <ArrowUpwardIcon fontSize="small" />}
          {sortDirection === 'desc' && <ArrowDownwardIcon fontSize="small" />}
        </div>
        <div style={{ flexGrow: 1 }} />
        <EveliTableColumnFilter filterItems={['filter 1']} />
        <EveliTableColumnOptions
          onChooseCols={() => setOpen(true)}
          onSortAsc={() => column.toggleSorting(false)}
          onSortDesc={() => column.toggleSorting(true)}
        />
      </div>
    </>
  )
}
