import React from 'react';
import { TablePagination } from '@mui/material';

import { TenantEntryDescriptor } from 'descriptor-dialob';
import Table from 'table';


type FormPaginationType = Table.TablePagination<TenantEntryDescriptor>;

export const DialobListPagination: React.FC<{
  state: FormPaginationType;
  setState: React.Dispatch<React.SetStateAction<FormPaginationType>>
}> = ({ state, setState }) => {


  function handleOnPageChange(_garbageEvent: any, newPage: number) {
    setState((prev) => prev.withPage(newPage));
  }

  function handleOnRowsPerPageChange(event: React.ChangeEvent<HTMLInputElement>) {
    setState((prev) => prev.withRowsPerPage(parseInt(event.target.value, 10)))
  }

  return (<TablePagination
    rowsPerPageOptions={state.rowsPerPageOptions}
    component="div"
    count={state.src.length}
    rowsPerPage={state.rowsPerPage}
    page={state.page}
    onPageChange={handleOnPageChange}
    onRowsPerPageChange={handleOnRowsPerPageChange} />);
}