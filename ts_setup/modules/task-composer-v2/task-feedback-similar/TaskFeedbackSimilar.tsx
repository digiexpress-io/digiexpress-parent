import React from 'react';
import { Chip, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { TaskCardStyleDefinition } from '../task-card';
import { FeedbackApi } from '@dxs-ts/task-feedback';

export interface TaskFeedbackSimilarProps {
  similarities: FeedbackApi.Similarity[] | undefined;
  style: TaskCardStyleDefinition;
  toggleShowSimilarities: () => void;
}

export const TaskFeedbackSimilar: React.FC<TaskFeedbackSimilarProps> = ({ similarities, style, toggleShowSimilarities }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  if (!similarities) {
    return (
      <TaskFeedbackSimilarRoot>
        <Typography color='error' sx={{ ...style.bodyTypography }}>{intl.formatMessage({ id: 'task.feedback.similarities.none' })}</Typography>
      </TaskFeedbackSimilarRoot>)
  }

  const label = similarities.length;

  return (
    <TaskFeedbackSimilarRoot className={classes.root} onClick={toggleShowSimilarities}>
      <Chip label={label} variant='filled' />
    </TaskFeedbackSimilarRoot>
  )
}


const MUI_NAME = 'TaskFeedbackSimilar';
const TaskFeedbackSimilarRoot = styled('div', {
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
