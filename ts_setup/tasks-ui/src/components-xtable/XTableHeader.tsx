import React from 'react';
import { TableSortLabel, Box } from '@mui/material';

import { visuallyHidden } from '@mui/utils';
import { XTableCell } from './XTableCell';
import { useXPref } from './XPrefContext';
import { useXTable } from './XTableContext';

export type SortType = 'asc' | 'desc'


const SortableHeader: React.FC<{ 
  dataId: string;
  defaultSort?: SortType;
  children: React.ReactNode;
  onSort: (key: string, direction: SortType) => void;
}> = ({ defaultSort, children, dataId, onSort }) => {

  const { pref, withSorting } = useXPref();
  const sorting = pref.getSorting();
  const active: boolean = (defaultSort && !sorting?.dataId) ? true : sorting?.dataId === dataId;
  const direction = defaultSort ? defaultSort : (sorting?.dataId === dataId && sorting.direction ? sorting.direction : undefined );

  function createSortHandler(_event: React.MouseEvent<unknown>) {
    const sortDirection = direction ? (direction === 'asc' ? 'desc' : 'asc') : 'asc' ;
    withSorting({ dataId, direction: sortDirection});
    onSort(dataId, sortDirection); 
  }

  return (<XTableCell align='left' padding='none' sortDirection={direction}>
    <TableSortLabel active={active} direction={direction} onClick={createSortHandler}>
      <>
        <b>{children}</b>
        {direction && (<Box component="span" sx={visuallyHidden}>{direction === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>)}
      </>
    </TableSortLabel>
  </XTableCell>);
}

export const XTableHeader: React.FC<{ 
  id: string;
  sortable?: boolean;
  defaultSort?: SortType;
  children: React.ReactNode;
  onSort?: (key: string, direction: SortType) => void;
}> = ({ sortable, children, id, defaultSort, onSort }) => {

  const { hiddenColumns } = useXTable();
  const { pref } = useXPref();
  const vis = pref.getVisibility(id);
  const hidden = vis?.enabled === false || (hiddenColumns && hiddenColumns.includes(id));

  if(hidden) {
    return null;
  }

  if(!sortable) {
    return (<XTableCell align='left' padding='none'>{children}</XTableCell>)
  }

  if(!onSort) {
    throw new Error("onSort must be defined");
  }

  return (<SortableHeader dataId={id} defaultSort={defaultSort} onSort={onSort}>{children}</SortableHeader>)
}