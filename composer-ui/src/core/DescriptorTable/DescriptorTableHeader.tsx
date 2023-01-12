import React from 'react';
import { alpha } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TablePagination from '@mui/material/TablePagination';
import TableRow from '@mui/material/TableRow';
import TableSortLabel from '@mui/material/TableSortLabel';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';

import { visuallyHidden } from '@mui/utils';


import DeClient from '@declient';
import { Order, DescriptorPagination } from './descriptor-pagination';


interface HeadCell {
  id: keyof DeClient.ServiceDescriptor;
}

const headCells: readonly HeadCell[] = [
  { id: 'name' },
  { id: 'desc' },
  { id: 'formId' },
  { id: 'flowId' },
  { id: 'id' },
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
            {headCell.id}
            {orderBy === headCell.id ? (<Box component="span" sx={visuallyHidden}>{order === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>) : null}
          </TableSortLabel>
        </TableCell>
      ))}
    </TableRow>
  );
}
export default DescriptorTableHeader;



