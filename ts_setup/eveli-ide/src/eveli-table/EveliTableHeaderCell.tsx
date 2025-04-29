import React from 'react';
import { Typography } from '@mui/material';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

import { Header } from '@tanstack/react-table';
import { EveliTableColumnSortAndChoose } from './EveliTableColumnSortAndChoose';
import { EveliTableFilterAndSearch } from './EveliTableFilterAndSearch';


type EveliTableHeaderProps = {
  children: React.ReactNode; // title
  header: Header<any, any>;
  onColumnFilter: () => void;
  onResetColVisibility: () => void;
}

export const EveliTableHeaderCell: React.FC<EveliTableHeaderProps> = (props) => {
  const { column } = props.header;
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

        <EveliTableFilterAndSearch header={props.header} />

        <EveliTableColumnSortAndChoose
          onChooseCols={props.onColumnFilter}
          onSortAsc={() => column.toggleSorting(false)}
          onSortDesc={() => column.toggleSorting(true)}
          onClearSorting={() => column.clearSorting()}
          onClearColVisibility={() => props.onResetColVisibility()}
        />
        <ColumnResizer header={props.header} />

      </div>
    )
  }
  return (
    <div className='headerCell' style={{ width: column.getSize() }}>
      <Typography>{props.children}</Typography>
      <div style={{ flexGrow: 1 }} />
      <EveliTableFilterAndSearch header={props.header} />
      <ColumnResizer header={props.header} />
    </div>

  )
}

const ColumnResizer: React.FC<{ header: Header<any, any> }> = ({ header }) => {
  const { column } = header;

  return (
    column.getCanResize() && (
      <div className='columnResizer'
        onMouseDown={header.getResizeHandler()}
        onTouchStart={header.getResizeHandler()}
      />
    )
  )
}
