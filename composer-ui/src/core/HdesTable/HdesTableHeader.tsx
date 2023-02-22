import React from 'react';

import Box from '@mui/material/Box';
import TableCell from '@mui/material/TableCell';
import TableRow from '@mui/material/TableRow';
import TableSortLabel from '@mui/material/TableSortLabel';

import { visuallyHidden } from '@mui/utils';
import { FormattedMessage } from 'react-intl';

import DeClient from '@declient';


interface HeadCell {
  id: keyof DeClient.HdesBodyEntity;
}

const headCells: readonly HeadCell[] = [
  { id: 'name' },
  { id: 'bodyType' },
];


const HdesTableHeader: React.FC<{
  content: DeClient.TablePagination<DeClient.HdesBodyEntity>,
  setContent: React.Dispatch<React.SetStateAction<DeClient.TablePagination<DeClient.HdesBodyEntity>>>
}> = ({ content, setContent }) => {

  const { order, orderBy } = content;

  const createSortHandler = (property: keyof DeClient.HdesBodyEntity) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => prev.withOrderBy(property))


  return (
    <TableRow sx={{ backgroundColor: 'table.dark'}}>
      {headCells.map((headCell) => (
        <TableCell key={headCell.id} align='left' padding='normal' sortDirection={orderBy === headCell.id ? order : false}>
          <TableSortLabel active={orderBy === headCell.id} direction={orderBy === headCell.id ? order : 'asc'} onClick={createSortHandler(headCell.id)}>
            <FormattedMessage id={`hdesTable.header.${headCell.id}`} />
            {orderBy === headCell.id ? (<Box component="span" sx={visuallyHidden}>{order === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>) : null}
          </TableSortLabel>
        </TableCell>
      ))}
    </TableRow>
  );
}
export default HdesTableHeader;



