import React from 'react';
import { alpha } from '@mui/material/styles';
import Box from '@mui/material/Box';
import TableCell from '@mui/material/TableCell';
import TableRow from '@mui/material/TableRow';
import TableSortLabel from '@mui/material/TableSortLabel';

import { visuallyHidden } from '@mui/utils';
import { FormattedMessage } from 'react-intl';

import DeClient from '@declient';
import { Order, DescriptorPagination } from './descriptor-pagination';


interface HeadCell {
  id: keyof DeClient.ServiceDescriptor;
}

const headCells: readonly HeadCell[] = [
  { id: 'name' },
  { id: 'formId' },
  { id: 'flowId' },
];


const DescriptorTableHeader: React.FC<{
  content: DescriptorPagination,
  setContent: React.Dispatch<React.SetStateAction<DescriptorPagination>>
}> = ({ content, setContent }) => {

  const { order, orderBy } = content;

  const createSortHandler = (property: keyof DeClient.ServiceDescriptor) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => prev.withOrderBy(property))

  return (
    <TableRow>
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



