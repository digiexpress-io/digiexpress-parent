import { generateUtilityClass, styled, Typography } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';

import composeClasses from '@mui/utils/composeClasses';
import React from 'react';


export const EveliTableColSelect: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const classes = useUtilityClasses();

  return (
    <EveliTableColSelectRoot className={classes.root}>
      {children}
    </EveliTableColSelectRoot>
  )
}



export const ColSelectItem: React.FC<{ colTitle: string }> = ({ colTitle }) => {
  const classes = useUtilityClasses();

  return (
    <ColSelectItemRoot className={classes.root}>
      <CheckBoxOutlineBlankIcon className='cols-select-checkmark-icon' />
      <Typography>{colTitle}</Typography>
    </ColSelectItemRoot>
  )
}



const DrawerColsRootClassName = 'ColSelectItemRoot';

const EveliTableColSelectRoot = styled('div', {
  name: DrawerColsRootClassName,
  slot: 'DrawerColSelect',
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


const ColSelectItemRoot = styled('div', {
  name: DrawerColsRootClassName,
  slot: 'ColSelectItem',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
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
    '.cols-select-checkmark-icon': {
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
  const getUtilityClass = (slot: string) => generateUtilityClass(DrawerColsRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
