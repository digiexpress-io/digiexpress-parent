import { generateUtilityClass, styled, Typography } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';

import composeClasses from '@mui/utils/composeClasses';
import React from 'react';


export const EveliTableRightMenuCols: React.FC = () => {
  const classes = useUtilityClasses();

  return (
    <EveliTableRightMenuColsRoot className={classes.root}>
      <ColItem colTitle='Priority' />
      <ColItem colTitle='Name' />
      <ColItem colTitle='Client' />
      <ColItem colTitle='Status' />
      <ColItem colTitle='Assignee' />
      <ColItem colTitle='Info' />
      <ColItem colTitle='Due' />
      <ColItem colTitle='Created' />
    </EveliTableRightMenuColsRoot>
  )
}



const ColItem: React.FC<{ colTitle: string }> = ({ colTitle }) => {
  const classes = useUtilityClasses();

  return (
    <ColItemRoot className={classes.root}>
      <CheckBoxOutlineBlankIcon className='cols-select-checkmark-icon' />
      <Typography>{colTitle}</Typography>
    </ColItemRoot>
  )
}



const ColsRootClassName = 'EveliTableRightMenuCols';

const EveliTableRightMenuColsRoot = styled('div', {
  name: ColsRootClassName,
  slot: 'RightMenuColumnsSelect',
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


const ColItemRoot = styled('div', {
  name: ColsRootClassName,
  slot: 'RightMenuColumnItem',
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
  const getUtilityClass = (slot: string) => generateUtilityClass(ColsRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
