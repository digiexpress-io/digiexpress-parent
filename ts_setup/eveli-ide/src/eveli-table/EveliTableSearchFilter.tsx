import React from 'react';
import { generateUtilityClass, InputAdornment, styled, TextField, Typography } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import composeClasses from '@mui/utils/composeClasses';
import { Column } from '@tanstack/react-table';

import { TaskApi } from '@/api-task';



interface EveliTableSearchFilterProps {
  column: Column<TaskApi.Task, unknown>;
}

export const EveliTableSearchFilter: React.FC<EveliTableSearchFilterProps> = ({ column }) => {
  const classes = useUtilityClasses();
  const filterValue = column.getFilterValue() as string ?? '';


  return (
    <EveliTableSearchFieldRoot className={classes.root}>
      <Typography>Filter {column.columnDef.header?.toString().toLowerCase()}</Typography>
      <TextField placeholder='Search' value={filterValue} onChange={(e) => column.setFilterValue(e.target.value)}
        slotProps={{
          input: {
            startAdornment: <InputAdornment position="start">
              <SearchIcon className='filters-adornment-icon' />
            </InputAdornment>
          }
        }}
      >
        {filterValue}
      </TextField>
    </EveliTableSearchFieldRoot>
  )
}


export const EveliTableSearchFieldRootClassName = 'EveliTableSearchField';

export const EveliTableSearchFieldRoot = styled('div', {
  name: EveliTableSearchFieldRootClassName,
  slot: 'Search',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },

})(({ theme }) => {
  return {
    '.MuiFormControl-root': {
      margin: theme.spacing(1),
    },
    '.MuiInputBase-root': {
      paddingLeft: 0,
      fontSize: '10pt',
      height: '2rem',
      minWidth: '100%'
    },
    '.MuiInputBase-input': {
      padding: theme.spacing(0.5),
    },
    '.filters-icon': {
      color: theme.palette.primary.main,
      fontSize: 'medium'
    },
    '.filters-adornment-icon': {
      marginLeft: theme.spacing(1),
      color: theme.palette.primary.main,
      fontSize: 'medium',
    },
    '.MuiTypography-root': {
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
      ...theme.typography.subtitle2,
    }
  }
});

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTableSearchFieldRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}