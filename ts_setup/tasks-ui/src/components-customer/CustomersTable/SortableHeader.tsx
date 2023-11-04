
import React from 'react';
import { TableCell, Box, TableSortLabel } from '@mui/material';
import { visuallyHidden } from '@mui/utils';
import { FormattedMessage } from 'react-intl';
import { ProjectDescriptor } from 'descriptor-project';
import Pagination from 'table';

const SortableHeader: React.FC<{
  id: keyof ProjectDescriptor,
  content: Pagination.TablePagination<ProjectDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<Pagination.TablePagination<ProjectDescriptor>>>
}> = ({ id, content, setContent }) => {

  const { order, orderBy } = content;

  const createSortHandler = (property: keyof ProjectDescriptor) =>
    (_event: React.MouseEvent<unknown>) => setContent(prev => prev.withOrderBy(property))

  return (
    <TableCell key={id} align='left' padding='none' sortDirection={orderBy === id ? order : false}>
      <TableSortLabel active={orderBy === id} direction={orderBy === id ? order : 'asc'} onClick={createSortHandler(id)}>
        <FormattedMessage id={`project.header.${id}`} />
        {orderBy === id ? (<Box component="span" sx={visuallyHidden}>{order === 'desc' ? 'sorted descending' : 'sorted ascending'}</Box>) : null}
      </TableSortLabel>
    </TableCell>
  );
}


const SortableHeaders: React.FC<{
  columns: (keyof ProjectDescriptor)[],
  content: Pagination.TablePagination<ProjectDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<Pagination.TablePagination<ProjectDescriptor>>>
}> = ({ columns, content, setContent }) => {

  return (<>{columns.map((id) => (<SortableHeader key={id} id={id} content={content} setContent={setContent} />))}</>);
}

export { SortableHeader, SortableHeaders };


