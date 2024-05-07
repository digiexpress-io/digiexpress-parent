import React from 'react';
import { TablePagination as MPagination } from '@mui/material';

import { useTasks } from 'descriptor-task';
import { SetTaskPagination, TaskPagination } from '../TableContext';


export const TablePagination: React.FC<{ state: TaskPagination, setState: SetTaskPagination }> = (props) => {
  const { loading } = useTasks();
  const { state, setState } = props;

  return loading ? null :
    (<MPagination
      rowsPerPageOptions={state.rowsPerPageOptions}
      component="div"
      count={state.src.length}
      rowsPerPage={state.rowsPerPage}
      page={state.page}
      onPageChange={(_event, newPage) => setState((state: TaskPagination) => state.withPage(newPage))}
      onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setState((state: TaskPagination) => state.withRowsPerPage(parseInt(event.target.value, 10)))}
    />) 
}