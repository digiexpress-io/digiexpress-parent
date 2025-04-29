import { generateUtilityClass, styled, Typography } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';

import composeClasses from '@mui/utils/composeClasses';
import React from 'react';


export const EveliTableColumnSelect: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const classes = useUtilityClasses();

  return (
    <EveliTableColumnSelectRoot className={classes.root}>
      {children}
    </EveliTableColumnSelectRoot>
  )
}


interface ColumnSelectItemProps {
  colTitle: string;
  isVisible: boolean;
  onToggle: () => void;
}

//TODO disabled state for cols that cannot be hidden
//https://tanstack.com/table/latest/docs/guide/column-visibility#column-visibility-toggle-apis
export const ColSelectItem: React.FC<ColumnSelectItemProps> = ({ colTitle, isVisible, onToggle }) => {
  const classes = useUtilityClasses();


  return (
    <ColumnSelectItemRoot className={classes.root} onClick={onToggle}>
      {isVisible ? <CheckBoxIcon className='cols-select-checkmark-icon' /> : <CheckBoxOutlineBlankIcon className='cols-select-checkmark-icon' />}
      <Typography>{colTitle}</Typography>
    </ColumnSelectItemRoot>
  )
}



const EveliTableColumnSelectRootClassName = 'ColumnSelectItemRoot';

const EveliTableColumnSelectRoot = styled('div', {
  name: EveliTableColumnSelectRootClassName,
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


const ColumnSelectItemRoot = styled('div', {
  name: EveliTableColumnSelectRootClassName,
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
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTableColumnSelectRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
