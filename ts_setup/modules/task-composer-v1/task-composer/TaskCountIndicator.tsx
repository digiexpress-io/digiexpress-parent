import React from 'react';
import { Avatar, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses/composeClasses';



export const TaskCountIndicator: React.FC<{ count: number | undefined }> = ({ count }) => {
  const classes = useUtilityClasses();


  return (
    <TaskCountIndicatorRoot className={classes.root}>
      <Typography>{count}</Typography>
    </TaskCountIndicatorRoot>
  )
}


export const MUI_NAME = 'TaskCountIndicator';
export interface TaskCountIndicatorClasses {
  root: string;
}

export type TaskCountIndicatorClassKey = keyof TaskCountIndicatorClasses;


export const TaskCountIndicatorRoot = styled(Avatar, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.hasCount
    ];
  },
})(({ theme }) => {

  return {
    width: '20pt',
    height: '20pt',
    backgroundColor: theme.palette.secondary.main,
    border: `1px solid ${theme.palette.divider}`,
    '&.TaskCountIndicator-root .MuiTypography-root': {
      color: theme.palette.text.primary,
      fontWeight: 'bold'
    }
  }
})

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    hasCount: ['hasCount']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
