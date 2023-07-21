import React from 'react';
import { Box, Button, TableHead, TableCell, TableRow, TableSortLabel, SxProps, Typography } from '@mui/material';

import { visuallyHidden } from '@mui/utils';
import { FormattedMessage, useIntl } from 'react-intl';

import client from '@taskclient';

interface HeadCell {
  id: keyof client.TaskDescriptor;
}

const headCells: readonly HeadCell[] = [
  { id: 'title' },
  { id: 'description' },
  { id: 'dueDate' }
];


const SortableHeader: React.FC<{
  id: keyof client.TaskDescriptor,
  content: client.TablePagination<client.TaskDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<client.TablePagination<client.TaskDescriptor>>>
}> = ({ id, content, setContent }) => {

  const { order, orderBy } = content;

  const createSortHandler = (property: keyof client.TaskDescriptor) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => prev.withOrderBy(property))

  return (
    <TableCell key={id} align='left' padding='none' sortDirection={orderBy === id ? order : false}>
      <TableSortLabel active={orderBy === id} direction={orderBy === id ? order : 'asc'} onClick={createSortHandler(id)}>
        <FormattedMessage id={`mywork.table.header.${id}`} />
        {orderBy === id ? (<Box component="span" sx={visuallyHidden}>{order === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>) : null}
      </TableSortLabel>
    </TableCell>
  );
}

const StyledSpotLight: React.FC<{ content: client.TablePagination<client.TaskDescriptor> }> = ({ content }) => {
  const sx: SxProps = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset" };

  return (<Button color="primary" variant="contained" sx={sx}>
    <Typography noWrap><FormattedMessage id={`mywork.table.header`} /></Typography>
  </Button>);
}

const DescriptorTableHeader: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<client.TablePagination<client.TaskDescriptor>>>
}> = ({ content, setContent }) => {

  return (
    <TableHead>
      <TableRow>
        <TableCell align='left' padding='none'><StyledSpotLight content={content} /></TableCell>
        {headCells.map((headCell) => (<SortableHeader key={headCell.id} id={headCell.id} content={content} setContent={setContent} />))}
      </TableRow>
    </TableHead>
  );
}
//border-top-right-radius
export default DescriptorTableHeader;



