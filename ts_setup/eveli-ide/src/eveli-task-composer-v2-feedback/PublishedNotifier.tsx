import React from 'react';
import { alpha, generateUtilityClass, styled } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import composeClasses from '@mui/utils/composeClasses';
import { TaskApi } from '@/api-task';
import { TaskCardStyleDefinition } from '../eveli-task-composer-v2-task-card';
import { useIntl } from 'react-intl';


export const PublishedNotifier: React.FC<{ task: TaskApi.Task, style?: TaskCardStyleDefinition }> = ({ task, style }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();



  return (
    <StyledPublishedNotifier className={classes.msgContainer} sx={{ ...style?.bodyTypographySmall }}>
      <CloseIcon />
      {intl.formatMessage({id: 'task.feedback.notPublished', defaultMessage: 'Not published'})}
    </StyledPublishedNotifier>
  )
}


const MUI_NAME = 'PublishedNotifier';
const StyledPublishedNotifier = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  backgroundColor: alpha(theme.palette.error.main, 0.1),
  padding: theme.spacing(0.5),
  borderRadius: theme.spacing(1),
  border: `1px solid ${theme.palette.error.main}`,
  color: theme.palette.error.main,
  fontSize: theme.typography.body2.fontSize,

  '.MuiSvgIcon-root': {
    fontSize: 'small',
    color: 'red',
    marginRight: theme.spacing(1)
  },

}));

export const useUtilityClasses = () => {
  const slots = {
    msgContainer: ['msgContainer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};
