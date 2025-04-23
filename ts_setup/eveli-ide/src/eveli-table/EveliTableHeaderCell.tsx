import React from 'react';
import { Typography } from '@mui/material';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

import { Header } from '@tanstack/react-table';
import { EveliTableColumnOptions } from './EveliTableColumnOptions';
import { EveliTableColumnFilter } from './EveliTableColumnFilter';


type EveliTableHeaderProps = {
  children: React.ReactNode; // title
  header: Header<any, any>;
  onColumnFilter: () => void;
  onResetColVisibility: () => void;
}

//TODO clean up of isFilterable / isSortable
export const EveliTableHeaderCell: React.FC<EveliTableHeaderProps> = (props) => {
  const { column } = props.header;
  const isFilterable = column.getCanFilter();
  const isSortable = column.getCanSort();
  const sortDirection = column.getIsSorted();


  if (isSortable) {
    return (
      <div className='headerCell' style={{ width: column.getSize() }}>
        <Typography>{props.children}</Typography>
        <div style={{ marginLeft: 4, display: 'flex' }}>
          {sortDirection === 'asc' && <ArrowUpwardIcon fontSize="small" />}
          {sortDirection === 'desc' && <ArrowDownwardIcon fontSize="small" />}
        </div>
        <div style={{ flexGrow: 1 }} />

        <EveliTableColumnFilter header={props.header} />

        <EveliTableColumnOptions
          onChooseCols={props.onColumnFilter}
          onSortAsc={() => column.toggleSorting(false)}
          onSortDesc={() => column.toggleSorting(true)}
          onClearSorting={() => column.clearSorting()}
          onClearColVisibility={() => props.onResetColVisibility()}
        />
      </div>
    )
  }
  return (
    <div className='headerCell' style={{ width: column.getSize() }}>
      <Typography>{props.children}</Typography>
      <div style={{ flexGrow: 1 }} />
      <EveliTableColumnFilter header={props.header} />
    </div>

  )
}
