import React from 'react';
import { IconButton, MenuItem, TextField, Typography } from '@mui/material';
import KeyboardArrowRightIcon from '@mui/icons-material/KeyboardArrowRight';
import KeyboardArrowLeftIcon from '@mui/icons-material/KeyboardArrowLeft';
import FirstPageIcon from '@mui/icons-material/FirstPage';
import LastPageIcon from '@mui/icons-material/LastPage';

import { Table } from '@tanstack/react-table';

import { useIntl } from 'react-intl';


interface EveliTablePaginationProps {
  table: Table<any>;
  data: unknown[],
  initialPageSize: number;
  pagination: {
    pageIndex: number;
    pageSize: number;
  }
}


export const Pagination: React.FC<EveliTablePaginationProps> = (props) => {
  const intl = useIntl();

  return (
    <>
      <Typography>{intl.formatMessage({ id: 'eveli.table.footer.tasks.total', defaultMessage: 'Total tasks: ' })} {props.data.length}</Typography>
      <Typography>{intl.formatMessage({ id: 'eveli.table.footer.rowsPerPage', defaultMessage: 'Rows per page: ' })}</Typography>
      <TextField select value={props.table.getState().pagination.pageSize} onChange={e => { props.table.setPageSize(Number(e.target.value)) }}>
        {[props.initialPageSize, 10, 20].map(pageSize => (
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

