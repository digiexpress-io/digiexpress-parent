import { Box, generateUtilityClass, styled, Typography } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';

import composeClasses from '@mui/utils/composeClasses';
import React from 'react';


export const EveliTableRightMenuFilters: React.FC = () => {
  const classes = useUtilityClasses();

  return (
    <EveliTableRightMenuFiltersRoot className={classes.root}>
      <EveliTableRightMenuFilterItem filterTitle='Status' />
      <EveliTableRightMenuFilterItem filterTitle='Priority' />
    </EveliTableRightMenuFiltersRoot>
  )
}



export const EveliTableRightMenuFilterItem: React.FC<{ filterTitle: string }> = ({ filterTitle }) => {
  const classes = useUtilityClasses();

  return (
    <EveliTableRightMenuFilterItemRoot className={classes.root}>
      <KeyboardArrowDownIcon className='filters-select-checkmark-icon' />
      <Typography>{filterTitle}</Typography>
    </EveliTableRightMenuFilterItemRoot>
  )
}



const EveliTableRightMenuFiltersRootClassName = 'EveliTableRightMenuFilters';

const EveliTableRightMenuFiltersRoot = styled('div', {
  name: EveliTableRightMenuFiltersRootClassName,
  slot: 'RightMenuFiltersSelect',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    width: '100%',
    padding: theme.spacing(1),
    display: 'flex',
    alignItems: 'left',
    justifySelf: 'flex-start',
    flexDirection: 'column'
  };
});


const EveliTableRightMenuFilterItemRoot = styled('div', {
  name: EveliTableRightMenuFiltersRootClassName,
  slot: 'RightMenuFilterItem',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: theme.spacing(1),
    '.MuiTypography-root': {
      fontSize: '10pt',
      fontWeight: 400,
    },
    '.filters-select-checkmark-icon': {
      marginLeft: theme.spacing(1),
      marginRight: theme.spacing(2),
      color: theme.palette.primary.main,
      fontSize: 'medium',
    },
  };
});

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTableRightMenuFiltersRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
