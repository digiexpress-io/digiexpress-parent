import React from 'react';

import { ArrowUpward as ArrowUpwardIcon } from '@mui/icons-material';
import { ArrowDownward as ArrowDownwardIcon } from '@mui/icons-material';

import { Header, Table } from '@tanstack/react-table';

import { ToolHeaderSearch } from '../tool-header-search';
import { ToolHeaderSort } from '../tool-header-sort';


export type ToolHeaderOptionsProps = {
  header: Header<any, any>;
  table: Table<any>
  onColumnFilter: () => void;
}

export const ToolHeaderOptions: React.FC<ToolHeaderOptionsProps> = (props) => {
  const { column } = props.header;
  const isSortable = column.getCanSort();
  const sortDirection = column.getIsSorted();


  if (isSortable) {
    return (
      <>
        <div style={{ marginLeft: 4, display: 'flex' }}>
          {sortDirection === 'asc' && <ArrowUpwardIcon fontSize="small" />}
          {sortDirection === 'desc' && <ArrowDownwardIcon fontSize="small" />}
        </div>
        <ToolHeaderSearch header={props.header} />

        <ToolHeaderSort
          onChooseCols={props.onColumnFilter}
          header={props.header}
          table={props.table}
        />
        <ColumnResizer header={props.header} />
      </>
    )
  }
  return (
    <>
      <div style={{ flexGrow: 1 }} />
      <ToolHeaderSearch header={props.header} />
      <ColumnResizer header={props.header} />
    </>

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
