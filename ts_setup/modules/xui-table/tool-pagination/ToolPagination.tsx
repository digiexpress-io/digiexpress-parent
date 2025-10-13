import React from 'react';
import { IconButton, MenuItem, TextField, Typography } from '@mui/material';
import { KeyboardArrowRight as KeyboardArrowRightIcon } from '@mui/icons-material';
import { KeyboardArrowLeft as KeyboardArrowLeftIcon } from '@mui/icons-material';
import { FirstPage as FirstPageIcon } from '@mui/icons-material';
import { LastPage as LastPageIcon } from '@mui/icons-material';

import { Table } from '@tanstack/react-table';

import { useIntl } from 'react-intl';


export interface ToolPaginationProps {
  table: Table<any>;
  initialPageSize: number;
  pagination: {
    pageIndex: number;
    pageSize: number;
  }
}


export const ToolPagination: React.FC<ToolPaginationProps> = (props) => {
  const intl = useIntl();
  const pageSizes = Array.from(new Set([props.initialPageSize, 10, 20])).sort();  

  return (
    <>
      <Typography>{intl.formatMessage({ id: 'eveli.table.footer.tasks.total', defaultMessage: 'Total tasks: ' })} {props.table.getRowCount()}</Typography>
      <Typography>{intl.formatMessage({ id: 'eveli.table.footer.rowsPerPage', defaultMessage: 'Rows per page: ' })}</Typography>
      <TextField select value={props.table.getState().pagination.pageSize} onChange={e => { props.table.setPageSize(Number(e.target.value)) }}>
        {pageSizes.map(pageSize => (
          <MenuItem key={pageSize} value={pageSize}>
            {pageSize}
          </MenuItem>
        ))}
      </TextField>

      <IconButton onClick={() => props.table.firstPage()} disabled={!props.table.getCanPreviousPage()}><FirstPageIcon /></IconButton>
      <IconButton onClick={() => props.table.previousPage()} disabled={!props.table.getCanPreviousPage()}><KeyboardArrowLeftIcon /></IconButton>


      <span style={{ marginLeft: 10, marginRight: 10 }}>
        <Typography>{intl.formatMessage({ id: 'eveli.table.footer.pageNumber', defaultMessage: 'Page ' })}
          {props.pagination.pageIndex + 1} / {props.table.getPageCount()}
        </Typography>
      </span>
      <IconButton onClick={() => props.table.nextPage()} disabled={!props.table.getCanNextPage()}><KeyboardArrowRightIcon /></IconButton>
      <IconButton onClick={() => props.table.lastPage()} disabled={!props.table.getCanNextPage()}><LastPageIcon /></IconButton>
    </>

  )
}

