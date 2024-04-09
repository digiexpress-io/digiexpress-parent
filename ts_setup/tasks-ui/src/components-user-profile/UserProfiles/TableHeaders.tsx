
import React from 'react';
import { TableCell, Box, TableSortLabel } from '@mui/material';
import { visuallyHidden } from '@mui/utils';
import { FormattedMessage } from 'react-intl';
import { UserProfileDescriptor } from 'descriptor-access-mgmt';
import Pagination from 'table';

const SortableHeader: React.FC<{
  id: keyof UserProfileDescriptor,
  content: Pagination.TablePagination<UserProfileDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<Pagination.TablePagination<UserProfileDescriptor>>>
}> = ({ id, content, setContent }) => {

  const { order, orderBy } = content;

  const createSortHandler = (property: keyof UserProfileDescriptor) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => prev.withOrderBy(property))

  return (
    <TableCell key={id} align='left' padding='none' sortDirection={orderBy === id ? order : false}>
      <TableSortLabel active={orderBy === id} direction={orderBy === id ? order : 'asc'} onClick={createSortHandler(id)}>
        <FormattedMessage id={`userprofileTable.header.${id}`} />
        {orderBy === id ? (<Box component="span" sx={visuallyHidden}>{order === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>) : null}
      </TableSortLabel>
    </TableCell>
  );
}


const SortableHeaders: React.FC<{
  columns: (keyof UserProfileDescriptor)[],
  content: Pagination.TablePagination<UserProfileDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<Pagination.TablePagination<UserProfileDescriptor>>>
}> = ({ columns, content, setContent }) => {

  return (<>{columns.map((id) => (<SortableHeader key={id} id={id} content={content} setContent={setContent} />))}</>);
}

export { SortableHeader, SortableHeaders };


