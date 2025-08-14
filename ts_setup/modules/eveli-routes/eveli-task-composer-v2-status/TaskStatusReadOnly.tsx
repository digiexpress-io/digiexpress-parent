import React from 'react';
import { Box, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { TaskCardStyleDefinition } from '../eveli-task-composer-v2-task-card';
import { useIntl } from 'react-intl';
import { useTaskDashboard } from '../eveli-task-composer-v2';
import { TaskApi } from '@dxs-ts/eveli-api';

const getStatusColor = (status: TaskApi.TaskStatus): string => {
  const colorEnum = TaskApi.task_status_colors[status];

  switch (colorEnum) {
    case TaskApi.Colors.RED:
      return '#f44336';
    case TaskApi.Colors.BLUE:
      return '#2196f3';
    case TaskApi.Colors.GREEN:
      return '#4caf50';
    case TaskApi.Colors.YELLOW:
      return '#ffeb3b';
    case TaskApi.Colors.GREY:
      return '#9e9e9e';
    default:
      return '#ccc';
  }
};


const TaskProgressBar: React.FC<{ status: TaskApi.TaskStatus, style: TaskCardStyleDefinition }> = ({ status, style }) => {
  const intl = useIntl();
  const { task } = useTaskDashboard();
  const classes = useUtilityClasses();

  const getProgress = (): number => {
    switch (status) {
      case 'NEW':
        return 25;
      case 'OPEN':
        return 50;
      case 'COMPLETED':
      case 'REJECTED':
        return 100;
      case 'TRANSFERRED':
        return 50;
      case 'DELEGATED':
        return 50;
      case 'WAITING':
        return 50;
      default:
        return 0;
    }
  };

  const progress = getProgress();
  const color = getStatusColor(status);


  return (
    <Box className={classes.progressBar}>
      <Box display='flex'>
      <Typography sx={{ ...style.bodyTypography, fontWeight: 500, mb: 1 }}>
        {intl.formatMessage({ id: 'task.status', defaultMessage: 'Status' })}
      </Typography>
      <Box flexGrow={1} />
      <Typography fontWeight={500} sx={{...style.bodyTypography, fontWeight: 'bold'}}>{task.status}</Typography>
      </Box>
      <Box className={classes.backgroundTrack}>
        <Box sx={{
          height: '100%',
          width: `${progress}%`,
          backgroundColor: color,
          transition: 'width 0.3s ease-in-out',
        }}
        />
      </Box>
      <Typography className={classes.progressDesc}>
        {progress}
        {intl.formatMessage({ id: 'task.status.percComplete', defaultMessage: '% complete' })}
      </Typography>
    </Box>
  );
};

export const TaskStatusReadOnly: React.FC<{ style: TaskCardStyleDefinition }> = ({ style }) => {
  const classes = useUtilityClasses();
  const { task } = useTaskDashboard();




  return (
    <StyledTaskStatusReadOnly className={classes.root}>
      <TaskProgressBar status={task.status!} style={style} />
    </StyledTaskStatusReadOnly>
  );
};

const MUI_NAME = 'TaskStatusReadOnly';
const StyledTaskStatusReadOnly = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",

    '& .TaskStatusReadOnly-progressBar': {
      width: '100%'
    },
    '& .TaskStatusReadOnly-backgroundTrack': {
      width: '100%',
      height: theme.spacing(2),
      backgroundColor: '#eee',
      borderRadius: theme.spacing(3),
      overflow: 'hidden',
      boxShadow: 'inset 0 1px 3px rgba(0,0,0,0.2)',
    },
    '& .TaskStatusReadOnly-progressDesc': {
      marginTop: theme.spacing(0.5),
      textAlign: 'right',
      ...theme.typography.caption
    }
  };
})


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    progressBar: ['progressBar'],
    backgroundTrack: ['backgroundTrack'],
    progressIndicator: ['progressIndicator'],
    progressDesc: ['progressDesc']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}