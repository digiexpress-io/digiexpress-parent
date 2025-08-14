import React from 'react';
import { Box, generateUtilityClass, Stack, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskCardStyleDefinition } from '../eveli-task-composer-v2-task-card';
import { useTaskDashboard } from '../eveli-task-composer-v2';
import { TaskApi } from '@dxs-ts/eveli-api';



type Priority = TaskApi.TaskPriority;

const getPriorityColor = (priority: Priority): string => {
  const colorEnum = TaskApi.task_priority_colors[priority];
  switch (colorEnum) {
    case TaskApi.Colors.RED:
      return '#f44336';
    case TaskApi.Colors.BLUE:
      return '#2196f3';
    case TaskApi.Colors.GREEN:
      return '#4caf50';
    default:
      return '#ccc';
  }
};

export const TaskPriorityReadOnly: React.FC<{ style: TaskCardStyleDefinition }> = ({ style }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { task } = useTaskDashboard();

  return (
    <StyledTaskPriorityReadOnly className={classes.root} style={style}>
      <Stack direction='column' width='100%'>
        <Typography fontWeight={500} mb={1} sx={{ ...style.bodyTypography, fontWeight: 500 }}>
          {intl.formatMessage({ id: 'task.priority', defaultMessage: 'Priority' })}
        </Typography>
        <Box className={classes.priorities}>
          {Object.entries(TaskApi.task_priority_messages).map(([key, message]) => {
            const level = key as Priority;
            const isActive = task.priority === level;
            return (
              <PriorityBox isActive={isActive} color={getPriorityColor(level)} key={level} >
                {intl.formatMessage(message)}
              </PriorityBox>
            );
          })}
        </Box>
      </Stack>
    </StyledTaskPriorityReadOnly>
  );
};



const MUI_NAME = 'TaskPriorityReadOnly';
const StyledTaskPriorityReadOnly = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{ style: TaskCardStyleDefinition }>(({ theme, style }) => {

  return {
    display: "flex",
    alignItems: "center",
    '& .TaskPriorityReadOnly-priorities': {
      width: '100%',
      display: 'flex',
      flexDirection: 'row',
      flexGrow: 1,
      ...style.bodyTypography
    }
  };
})

const PriorityBox = styled(Box)<{ isActive: boolean; color: string }>(({ theme, isActive, color }) => ({
  padding: theme.spacing(0.5),
  marginLeft: theme.spacing(1),
  marginRight: theme.spacing(1),
  width: '33%',
  textAlign: 'center',
  border: `1px solid ${isActive ? color : theme.palette.divider}`,
  borderRadius: theme.spacing(3),
  backgroundColor: isActive ? color : 'transparent',
  color: isActive ? `${theme.palette.background.default}` : `${theme.palette.text.primary}`,
  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)'

}));

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    priorities: ['priorities'],
    onePriority: ['onePriority']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
