
import React from 'react';
import { TableCell, Box, TableSortLabel } from '@mui/material';
import { visuallyHidden } from '@mui/utils';
import { FormattedMessage } from 'react-intl';
import { CustomerDescriptor } from 'descriptor-customer';
import Pagination from 'table';

const SortableHeader: React.FC<{
  id: keyof CustomerDescriptor,
  content: Pagination.TablePagination<CustomerDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<Pagination.TablePagination<CustomerDescriptor>>>
}> = ({ id, content, setContent }) => {

  const { order, orderBy } = content;

  const createSortHandler = (property: keyof CustomerDescriptor) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => prev.withOrderBy(property))

  return (
    <TableCell key={id} align='left' padding='none' sortDirection={orderBy === id ? order : false}>
      <TableSortLabel active={orderBy === id} direction={orderBy === id ? order : 'asc'} onClick={createSortHandler(id)}>
        <FormattedMessage id={`customertable.header.${id}`} />
        {orderBy === id ? (<Box component="span" sx={visuallyHidden}>{order === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>) : null}
      </TableSortLabel>
    </TableCell>
  );
}


const SortableHeaders: React.FC<{
  columns: (keyof CustomerDescriptor)[],
  content: Pagination.TablePagination<CustomerDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<Pagination.TablePagination<CustomerDescriptor>>>
}> = ({ columns, content, setContent }) => {

  return (<>{columns.map((id) => (<SortableHeader key={id} id={id} content={content} setContent={setContent} />))}</>);
}

export { SortableHeader, SortableHeaders };


