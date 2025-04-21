import React from 'react';
import { Typography } from '@mui/material';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

import { EveliTableRoot, useUtilityClasses } from './useUtilityClasses';


import { EveliTableColumnFilter } from './EveliTableColumnFilter';
import { EveliTableColumnOptions } from './EveliTableColumnOptions';
import { EveliTableColumnFilterDialog } from './EveliTableColumnFilterDialog';
import { Column } from '@tanstack/react-table';

type EveliTableHeaderProps<T> = {
  children: React.ReactNode;
  column: Column<T, unknown>;
  sortDirection?: false | 'asc' | 'desc';
}

export const EveliTable: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const classes = useUtilityClasses();
  return (
    <EveliTableRoot className={classes.root}>
      {children}
    </EveliTableRoot>
  )
}
// for testing
export const EveliTableHeaderCell1 = <T,>({ children, column }: EveliTableHeaderProps<T>) => {
  const [open, setOpen] = React.useState(false);
  const sortDirection = column.getIsSorted();
  const isSortable = column.getCanSort();

  if (!isSortable) {
    return (
      <div className='headerCell' style={{ width: column.getSize() }}>
        <Typography>{children}</Typography>
        <div style={{ flexGrow: 1 }} />
        <EveliTableColumnFilter filterItems={['filter 1']} />
      </div>
    )
  }
  return (
    <>
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


export const EveliTableCell: React.FC<{ children?: React.ReactNode | string, width: number }> = ({ children, width }) => {
  return (
    <div className='rowCell' style={{ width }}>
      {(typeof children) === 'string' ? <Typography>{children}</Typography> : children}
    </div>
  )
}



// original
export const EveliTableHeaderCell: React.FC<{ children: string, filterItems: string[] }> = ({ children, filterItems }) => {
  const [open, setOpen] = React.useState(false);

  return (<>
    <EveliTableColumnFilterDialog open={open} onClose={() => setOpen(false)} />
    <div className='headerCell'>
      <Typography>{children}</Typography>
      <div style={{ flexGrow: 1 }} />
      <EveliTableColumnFilter filterItems={filterItems} />
      <EveliTableColumnOptions onChooseCols={() => setOpen(true)} onSortAsc={() => { }} onSortDesc={() => { }} />
    </div>
  </>
  )
}
