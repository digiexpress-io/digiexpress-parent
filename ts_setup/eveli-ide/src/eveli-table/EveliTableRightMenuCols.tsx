import { Box, generateUtilityClass, styled, Typography } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';

import composeClasses from '@mui/utils/composeClasses';
import React from 'react';


export const EveliTableRightMenuCols: React.FC = () => {
  const classes = useUtilityClasses();

  return (
    <EveliTableRightMenuColsRoot className={classes.root}>
      <EveliTableRightMenuColItem colTitle='Header cell 1' />
      <EveliTableRightMenuColItem colTitle='Header' />
      <EveliTableRightMenuColItem colTitle='Header cell 3' />
    </EveliTableRightMenuColsRoot>
  )
}



export const EveliTableRightMenuColItem: React.FC<{ colTitle: string }> = ({ colTitle }) => {
  const classes = useUtilityClasses();

  return (
    <EveliTableRightMenuColItemRoot className={classes.root}>
      <CheckBoxOutlineBlankIcon className='cols-select-checkmark-icon' />
      <Typography>{colTitle}</Typography>
    </EveliTableRightMenuColItemRoot>
  )
}



const EveliTableRightMenuColsRootClassName = 'EveliTableRightMenuCols';

const EveliTableRightMenuColsRoot = styled('div', {
  name: EveliTableRightMenuColsRootClassName,
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


const EveliTableRightMenuColItemRoot = styled('div', {
  name: EveliTableRightMenuColsRootClassName,
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
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTableRightMenuColsRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
