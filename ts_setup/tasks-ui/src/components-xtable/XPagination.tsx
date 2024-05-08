import React from 'react';
import { TablePagination as MPagination, Box } from '@mui/material';
import Table from 'table';


export interface XPaginationProps {
  state: Table.TablePagination<any>,
  setState: React.Dispatch<React.SetStateAction<any>>
}

export const XPagination: React.FC<XPaginationProps> = (props) => {
  const { state, setState } = props;

  return (
    <Box display='flex'>
      <Box alignSelf="center" flexGrow={1}></Box>
      <MPagination
        rowsPerPageOptions={state.rowsPerPageOptions}
        component="div"
        count={state.src.length}
        rowsPerPage={state.rowsPerPage}
        page={state.page}
        onPageChange={(_event, newPage) => setState((state: Table.TablePagination<any>) => state.withPage(newPage))}
        onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setState((state: Table.TablePagination<any>) => state.withRowsPerPage(parseInt(event.target.value, 10)))}
      />
    </Box>)
}