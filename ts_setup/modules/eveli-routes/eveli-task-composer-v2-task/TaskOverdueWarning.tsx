import React from 'react';
import { alpha, Typography, generateUtilityClass, styled, Theme } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import ErrorOutlinedIcon from '@mui/icons-material/ErrorOutlined';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { TaskCardStyleDefinition } from '../eveli-task-composer-v2-task-card';
import { TaskApi } from '@dxs-ts/eveli-api';


export interface TaskOverdueWarningProps {
  task: TaskApi.Task,
  style: TaskCardStyleDefinition
}

type OverdueStatus = 'overdue' | 'dueToday' | 'upcomingDue' | 'completedOverdue' | 'completedOnTime';

function getTaskOverdue(task: TaskApi.Task): { isOverdue: boolean, days: number, status: OverdueStatus } | undefined {

  if (!task.dueDate) {
    return;
  }

  const today = DateTime.local().startOf('day');
  const dueDate = DateTime.fromJSDate(task.dueDate).startOf('day');
  const diffInDays = Math.floor(today.diff(dueDate, 'days').days);

  if (task.status === 'COMPLETED' || task.status === 'REJECTED') {
    return {
      isOverdue: diffInDays > 0,
      days: Math.abs(diffInDays),
      status: diffInDays > 0 ? 'completedOverdue' : 'completedOnTime',
    }
  }

  if (diffInDays > 0) {
    return {
      isOverdue: true,
      days: diffInDays,
      status: 'overdue'
    }
  }
  else if (diffInDays === 0) {
    return {
      isOverdue: false,
      days: 0,
      status: 'dueToday'
    }
  } else {
    return {
      isOverdue: false,
      days: Math.abs(diffInDays),
      status: 'upcomingDue'
    }
  }
}



export const TaskOverdueWarning: React.FC<TaskOverdueWarningProps> = ({ ...props }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  const taskInfo = getTaskOverdue(props.task);

  if (!taskInfo) {
    return undefined;
  }


  switch (taskInfo?.status) {
    case 'upcomingDue':
      return (
        <StyledTaskOverrdueWarning className={classes.root} ownerState={{ ...props, status: taskInfo.status }}>
          <ErrorOutlinedIcon />
          <Typography textAlign='center' sx={{ ...props.style.bodyTypographySmall }}>
            {intl.formatMessage({ id: 'task.overdue.daysLeft', defaultMessage: `${taskInfo.days} day(s) left to complete task` })}
          </Typography>
        </StyledTaskOverrdueWarning>
      );

    case 'dueToday':
      return (
        <StyledTaskOverrdueWarning className={classes.root} ownerState={{ ...props, status: taskInfo.status }}>
          <ErrorOutlinedIcon />
          <Typography textAlign='center' sx={{ ...props.style.bodyTypographySmall ?? undefined }}>
            {intl.formatMessage({ id: 'task.overdue.dueToday', defaultMessage: 'Task is due today!' })}
          </Typography>
        </StyledTaskOverrdueWarning>
      )

    case 'completedOverdue':
      return (
        <StyledTaskOverrdueWarning className={classes.root} ownerState={{ ...props, status: taskInfo.status }}>
          <ErrorOutlinedIcon />
          <Typography sx={{ ...props.style.bodyTypographySmall }}>
            {intl.formatMessage({ id: 'task.overdue.closedOverdue', defaultMessage: `Task was closed ${taskInfo.days} day(s) overdue` })}
          </Typography>
        </StyledTaskOverrdueWarning>
      )

    case 'overdue':
      return (
        <StyledTaskOverrdueWarning className={classes.root} ownerState={{ ...props, status: taskInfo.status }}>
          <ErrorOutlinedIcon />
          <Typography sx={{ ...props.style.bodyTypographySmall }}>
            {intl.formatMessage({ id: 'task.overdue', defaultMessage: `Task is ${taskInfo.days} day(s) overdue` })}
          </Typography>
        </StyledTaskOverrdueWarning>
      )
    default:
      return (<></>)
  }
}



const MUI_NAME = 'TaskOverrdueWarningClassName'
const StyledTaskOverrdueWarning = styled('div', {
  name: MUI_NAME,
  slot: 'Priority',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{ ownerState: TaskOverdueWarningProps & { status: OverdueStatus } }>(({ theme, ownerState }) => {

  return {
    display: 'flex',
    alignItems: 'center',
    backgroundColor: alpha(getBackgroundColor(theme, ownerState.status), 0.1),
    padding: theme.spacing(0.5),
    borderRadius: theme.spacing(1),
    border: `1px solid ${getBackgroundColor(theme, ownerState.status)}`,
    color: getBackgroundColor(theme, ownerState.status),

    '& .MuiSvgIcon-root': {
      color: getBackgroundColor(theme, ownerState.status),
      fontSize: 'medium',
      marginRight: theme.spacing(0.5)
    }
  };
});

const getBackgroundColor = (theme: Theme, status: OverdueStatus) => {
  switch (status) {
    case 'overdue':
      return theme.palette.error.main;
    case 'dueToday':
      return '#e85d04';
    case 'upcomingDue':
      return theme.palette.info.main;
    case 'completedOverdue':
      return theme.palette.warning.main;
    case 'completedOnTime':
      return theme.palette.success.main;
    default:
      return theme.palette.background.paper;
  }
};

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}