import React from 'react';
import { TableCell as MTableCell, TableSortLabel, Box } from '@mui/material';

import { visuallyHidden } from '@mui/utils';
import { useTaskPrefs, ColumnName, SetTaskPagination, TaskPagination, getPrefSortId } from '../TableContext';
import { StyledTableCell } from 'components-generic';



export const TableHeader: React.FC<{ 
  name: ColumnName;
  classifierValue: string;
  sortable: boolean;
  children: React.ReactNode;
  setContent: SetTaskPagination;
  content: TaskPagination;
}> = ({ name, classifierValue, sortable, children, setContent, content }) => {

  const { pref, withSorting } = useTaskPrefs();
  const vis = pref.getVisibility(name);
  const hidden = vis?.enabled !== true;

  if(hidden) {
    return null;
  }

  if(!sortable) {
    return (<MTableCell align='left' padding='none'>{children}</MTableCell>)
  }

  const createSortHandler = (property: ColumnName) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => {
      const dataId = getPrefSortId(classifierValue, name);
      const next = prev.withOrderBy(property);
      withSorting({ dataId, direction: next.order });
      return next;
    })

  const active = content.orderBy === name;
  const sortDirection = active ? content.order : false;

  return (<StyledTableCell align='left' padding='none' sortDirection={sortDirection}>
    <TableSortLabel active={active} direction={active ? content.order : 'asc'} onClick={createSortHandler(name)}>
      <>
        <b>{children}</b>
        {active ? (<Box component="span" sx={visuallyHidden}>{content.order === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>) : null}
      </>
    </TableSortLabel>
  </StyledTableCell>);
}
