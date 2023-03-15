import React from 'react';
import { alpha } from '@mui/material/styles';
import Box from '@mui/material/Box';
import TableCell from '@mui/material/TableCell';
import TableRow from '@mui/material/TableRow';
import TableSortLabel from '@mui/material/TableSortLabel';

import { visuallyHidden } from '@mui/utils';
import { FormattedMessage } from 'react-intl';

import client from '@taskclient';


interface HeadCell {
  id: keyof client.TaskDescriptor;
}

const headCells: readonly HeadCell[] = [
  { id: 'subject' },
  { id: 'priority' },
  { id: 'status' },
  { id: 'owner' },
  { id: 'duedate' },
  { id: 'roles' },
];


const DescriptorTableHeader: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<client.TablePagination<client.TaskDescriptor>>>
}> = ({ content, setContent }) => {

  const { order, orderBy } = content;

  const createSortHandler = (property: keyof client.TaskDescriptor) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => prev.withOrderBy(property))

  return (
    <TableRow sx={{ backgroundColor: 'table.dark'}}>
      {headCells.map((headCell) => (
        <TableCell key={headCell.id} align='left' padding='normal' sortDirection={orderBy === headCell.id ? order : false}>
          <TableSortLabel active={orderBy === headCell.id} direction={orderBy === headCell.id ? order : 'asc'} onClick={createSortHandler(headCell.id)}>
            
            <FormattedMessage id={`descriptorTable.header.${headCell.id}`} />
            {orderBy === headCell.id ? (<Box component="span" sx={visuallyHidden}>{order === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>) : null}
          </TableSortLabel>
        </TableCell>
      ))}

      <TableCell align='left' padding='normal'>
        <FormattedMessage id='descriptorTable.header.articles' />
      </TableCell>
    </TableRow>
  );
}
export default DescriptorTableHeader;



