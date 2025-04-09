import { Box, generateUtilityClass, styled, Typography } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';

import composeClasses from '@mui/utils/composeClasses';
import React from 'react';
import { IndicatorStatus } from './IndicatorStatus';
import { IndicatorPriority } from './IndicatorPriority';


export const EveliTableRightMenuFilters: React.FC = () => {
  const classes = useUtilityClasses();
  const [statusOpen, setStatusOpen] = React.useState(false);
  const [filtersOpen, setFiltersOpen] = React.useState(false);

  function toggleStatus() {
    setStatusOpen(prev => !prev)
  }
  function toggleFilters() {
    setFiltersOpen(prev => !prev)
  }


  return (
    <RightMenuFiltersRoot className={classes.root}>

      <FilterItem filterTitle='Status' onClick={toggleStatus}>
        {statusOpen && (
          <FilterChildren className={classes.root}>
            <IndicatorStatus type='NEW' />
            <IndicatorStatus type='OPEN' />
            <IndicatorStatus type='COMPLETED' />
            <IndicatorStatus type='REJECTED' />
          </FilterChildren>
        )}
      </FilterItem>

      <FilterItem filterTitle='Priority' onClick={toggleFilters}>
        {filtersOpen && (
          <FilterChildren className={classes.root}>
            <IndicatorPriority type='LOW' />
            <IndicatorPriority type='MEDIUM' />
            <IndicatorPriority type='HIGH' />
          </FilterChildren>
        )}
      </FilterItem>
    </RightMenuFiltersRoot>
  )
}



export const FilterItem: React.FC<{ filterTitle: string, children: React.ReactNode, onClick: () => void }> = ({ filterTitle, children, onClick }) => {
  const classes = useUtilityClasses();

  return (
    <FilterItemRoot className={classes.root} onClick={onClick}>
      <Box className='filters-icon-alignment'>
        <KeyboardArrowDownIcon className='filters-select-checkmark-icon' />
        <Typography className='filters-title-typography'>{filterTitle}</Typography>
      </Box>
      {children}
    </FilterItemRoot>
  )
}

const FiltersRootClassName = 'EveliTableRightMenuFilters';

const FilterChildren = styled('div', {
  name: FiltersRootClassName,
  slot: 'FilterChild',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    display: 'flex',
    gap: theme.spacing(1),
    flexDirection: 'column',
    padding: theme.spacing(0.5),
    marginLeft: theme.spacing(5),
  };
});

const RightMenuFiltersRoot = styled('div', {
  name: FiltersRootClassName,
  slot: 'RightMenuFiltersSelect',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    padding: theme.spacing(1),
    alignItems: 'left',
    justifySelf: 'flex-start',
    fontSize: '10pt',
    '& > *:not(:first-child)': {
      marginTop: theme.spacing(1)
    },
  };
});


const FilterItemRoot = styled('div', {
  name: FiltersRootClassName,
  slot: 'RightMenuFilterItem',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    '.filters-select-checkmark-icon': {
      marginLeft: theme.spacing(1),
      marginRight: theme.spacing(2),
      color: theme.palette.primary.main,
      fontSize: 'medium',
    },
    '.filters-icon-alignment': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
    },
    '.filters-title-typography': {
      fontSize: '10pt'
    },
    ':hover': {
      cursor: 'pointer'
    },

  };
});

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(FiltersRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
