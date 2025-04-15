import { generateUtilityClass, styled, Typography } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';

import composeClasses from '@mui/utils/composeClasses';
import React from 'react';


export const EveliTableDrawerCols: React.FC = () => {
  const classes = useUtilityClasses();

  return (
    <EveliTableDrawerColsRoot className={classes.root}>
      <DrawerColItem colTitle='Priority' />
      <DrawerColItem colTitle='Name' />
      <DrawerColItem colTitle='Client' />
      <DrawerColItem colTitle='Status' />
      <DrawerColItem colTitle='Assignee' />
      <DrawerColItem colTitle='Info' />
      <DrawerColItem colTitle='Due' />
      <DrawerColItem colTitle='Created' />
    </EveliTableDrawerColsRoot>
  )
}



const DrawerColItem: React.FC<{ colTitle: string }> = ({ colTitle }) => {
  const classes = useUtilityClasses();

  return (
    <DrawerColItemRoot className={classes.root}>
      <CheckBoxOutlineBlankIcon className='cols-select-checkmark-icon' />
      <Typography>{colTitle}</Typography>
    </DrawerColItemRoot>
  )
}



const DrawerColsRootClassName = 'DrawerColsRoot';

const EveliTableDrawerColsRoot = styled('div', {
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


const DrawerColItemRoot = styled('div', {
  name: DrawerColsRootClassName,
  slot: 'DrawerColItem',
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
