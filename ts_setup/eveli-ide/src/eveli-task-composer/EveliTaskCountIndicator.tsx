import React from 'react';
import { Avatar, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses/composeClasses';



export const EveliTaskCountIndicator: React.FC<{ count: number | undefined }> = ({ count }) => {
  const classes = useUtilityClasses();


  return (
    <EveliTaskCountIndicatorRoot className={classes.root}>
      <Typography>{count}</Typography>
    </EveliTaskCountIndicatorRoot>
  )
}


export const MUI_NAME = 'EveliTaskCountIndicator';
export interface EveliTaskCountIndicatorClasses {
  root: string;
}

export type EveliTaskCountIndicatorClassKey = keyof EveliTaskCountIndicatorClasses;


export const EveliTaskCountIndicatorRoot = styled(Avatar, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {
    width: '20pt',
    height: '20pt',
    backgroundColor: theme.palette.secondary.main,
    border: `1px solid ${theme.palette.divider}`,
    '&.EveliTaskCountIndicator-root .MuiTypography-root': {
      color: theme.palette.text.primary,
      fontWeight: 'bold'
    }
  }
})

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
