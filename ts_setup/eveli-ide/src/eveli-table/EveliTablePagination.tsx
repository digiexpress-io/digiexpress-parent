import React from 'react';
import { generateUtilityClass, IconButton, MenuItem, styled, TextField, Typography } from '@mui/material';
import KeyboardArrowRightIcon from '@mui/icons-material/KeyboardArrowRight';
import KeyboardArrowLeftIcon from '@mui/icons-material/KeyboardArrowLeft';
import FirstPageIcon from '@mui/icons-material/FirstPage';
import LastPageIcon from '@mui/icons-material/LastPage';

import { Table } from '@tanstack/react-table';

import { TaskApi } from '@/api-task';
import composeClasses from '@mui/utils/composeClasses';


interface EveliTablePaginationProps {
  table: Table<TaskApi.Task>;
  data: TaskApi.Task[],
  initialPageSize: number;
  pagination: {
    pageIndex: number;
    pageSize: number;
  }
}

/*

      <select value={props.table.getState().pagination.pageSize} onChange={e => { props.table.setPageSize(Number(e.target.value)) }}>
        {[props.initialPageSize, 10, 20, 30, 40, 50].map(pageSize => (
          <option key={pageSize} value={pageSize}>
            {pageSize}
          </option>
        ))}
      </select>

*/

export const EveliTablePagination: React.FC<EveliTablePaginationProps> = (props) => {
  const classes = useUtilityClasses();

  return (
    <EveliTablePaginationRoot className={classes.root}>
      <Typography>Total tasks: {props.data.length}</Typography>

      <Typography>Rows per page:</Typography>
      <TextField select value={props.table.getState().pagination.pageSize} onChange={e => { props.table.setPageSize(Number(e.target.value)) }}>
        {[props.initialPageSize, 10, 20].map(pageSize => (
          <MenuItem key={pageSize} value={pageSize}>
            {pageSize}
          </MenuItem>
        ))}
      </TextField>

      <IconButton onClick={() => props.table.firstPage()} disabled={!props.table.getCanPreviousPage()}><FirstPageIcon /></IconButton>
      <IconButton onClick={() => props.table.previousPage()} disabled={!props.table.getCanPreviousPage()}><KeyboardArrowLeftIcon /></IconButton>
      <span style={{ marginLeft: 10, marginRight: 10 }}><Typography>Page {props.pagination.pageIndex + 1} of {props.table.getPageCount()}</Typography> </span>
      <IconButton onClick={() => props.table.nextPage()} disabled={!props.table.getCanNextPage()}><KeyboardArrowRightIcon /></IconButton>
      <IconButton onClick={() => props.table.lastPage()} disabled={!props.table.getCanNextPage()}><LastPageIcon /></IconButton>

    </EveliTablePaginationRoot>

  )
}


export const EveliTablePaginationClassName = 'EveliTablePagination';


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTablePaginationClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const EveliTablePaginationRoot = styled('div', {
  name: EveliTablePaginationClassName,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{}>(({ theme }) => {

  return {
    display: 'flex',
    justifyContent: 'flex-end',
    alignItems: 'center',
    fontSize: '10pt',
    width: '100%',
    borderRadius: `0px 0px 0px ${theme.spacing(1)}`,
    border: `1px solid ${theme.palette.divider}`,
    borderTop: 'unset',
    padding: theme.spacing(0.5),
    '.MuiTypography-root': {
      ...theme.typography.subtitle2,
      marginLeft: theme.spacing(1),
      marginRight: theme.spacing(1)
    },
    '.MuiIconButton-root': {
      '.MuiSvgIcon-root': {
        color: theme.palette.primary.main,
      },
      '&.Mui-disabled .MuiSvgIcon-root': {
        color: theme.palette.action.disabled
      },
    },
    '.MuiFormControl-root.MuiTextField-root': {
      marginTop: '0px'
    },
    '.MuiTextField-root .MuiInputBase-input': {
      paddingLeft: theme.spacing(2),
      paddingTop: theme.spacing(0.5),
      paddingBottom: theme.spacing(0.5)
    }
  };
});