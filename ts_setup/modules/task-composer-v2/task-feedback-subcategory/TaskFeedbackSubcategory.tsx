import React from 'react';
import { Chip, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { TaskCardStyleDefinition } from '../task-card';

export interface TaskFeedbackSubcategoryProps {
  subcategory: string | undefined;
  style: TaskCardStyleDefinition
}

export const TaskFeedbackSubcategory: React.FC<TaskFeedbackSubcategoryProps> = ({ subcategory, style }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  if (!subcategory) {
    return (
      <TaskFeedbackSubcategoryRoot>
        <Typography color='error' sx={{ ...style.bodyTypography }}>{intl.formatMessage({ id: 'task.subcategory.none', defaultMessage: 'No subcategory detected' })}</Typography>
      </TaskFeedbackSubcategoryRoot>)
  }

  const label = subcategory.charAt(0).toUpperCase() + subcategory.slice(1).toLowerCase().replace(/_/g, ' ');

  return (
    <TaskFeedbackSubcategoryRoot className={classes.root}>
      <Chip label={label} variant='filled' />
    </TaskFeedbackSubcategoryRoot>
  )
}


const MUI_NAME = 'TaskFeedbackSubcategory';
const TaskFeedbackSubcategoryRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    display: 'flex',
    alignItems: 'center',
    flexWrap: 'wrap',
    marginBottom: theme.spacing(1),
    gap: theme.spacing(1),

    '& .MuiChip-root': {
      backgroundColor: theme.palette.primary.main,
      color: theme.palette.primary.contrastText,
    }
  };
})


export const useUtilityClasses = () => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
