import React from 'react';
import { generateUtilityClass, InputAdornment, styled, TextField } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import composeClasses from '@mui/utils/composeClasses';


export const EveliTableSearchField: React.FC<{}> = () => {
  const classes = useUtilityClasses();


  return (
    <EveliTableSearchFieldRoot className={classes.root}>
    <TextField placeholder='Search' slotProps={{
      input: {
        startAdornment: <InputAdornment position="start">
          <SearchIcon className='filters-adornment-icon' />
        </InputAdornment>
      }
    }}
    >
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

})(({ theme}) => {
  return {
    '.MuiFormControl-root': {
      margin: theme.spacing(1),
    },
    '.MuiInputBase-root': {
      paddingLeft: 0,
      fontSize: '10pt',
      height: '2rem',

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
  }
});

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTableSearchFieldRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}