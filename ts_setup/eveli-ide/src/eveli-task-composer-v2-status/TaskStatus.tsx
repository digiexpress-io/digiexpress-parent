import React from 'react';
import { Box, generateUtilityClass, MenuItem, Select, SelectChangeEvent, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { TaskCardStyleDefinition } from '../eveli-task-composer-v2-task-card';
import { useIntl } from 'react-intl';


type TaskStatusType = 'NEW' | 'OPEN' | 'COMPLETED' | 'REJECTED';


const TaskProgressBar: React.FC<{ status: TaskStatusType, style: TaskCardStyleDefinition }> = ({ status, style }) => {
  const intl = useIntl();
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
      default:
        return 0;
    }
  };

  const getColor = (): string => {
    switch (status) {
      case 'NEW':
        return '#ffeb3b'; // Purpley
      case 'OPEN':
        return '#2196f3'; // Blue
      case 'COMPLETED':
        return '#4caf50'; // Green
      case 'REJECTED':
        return '#f44336'; // Red
      default:
        return '#ccc';
    }
  };

  const progress = getProgress();
  const color = getColor();


  return (
    <Box className={classes.progressBar}>
      <Typography sx={{ ...style.bodyTypography, fontWeight: 500, mb: 1 }}>
        {intl.formatMessage({ id: 'task.status', defaultMessage: 'Status' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {status}
      </Typography>
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

export const TaskStatus: React.FC<{ style: TaskCardStyleDefinition }> = ({ style }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const [status, setStatus] = React.useState<TaskStatusType>('NEW');

  const handleStatusChange = (e: SelectChangeEvent) => {
    setStatus(e.target.value as TaskStatusType);
  };

  return (
    <StyledTaskStatus className={classes.root}>
      <TaskProgressBar status={status} style={style} />

      <Select
        value={status}
        onChange={handleStatusChange}
        size="small"
        sx={{
          minWidth: 120,
          maxWidth: 150,
          alignSelf: 'center',
        }}
      >
        <MenuItem value="NEW">{intl.formatMessage({ id: 'task.status.new' })}</MenuItem>
        <MenuItem value="OPEN">{intl.formatMessage({ id: 'task.status.open' })}</MenuItem>
        <MenuItem value="COMPLETED">{intl.formatMessage({ id: 'task.status.completed' })}</MenuItem>
        <MenuItem value="REJECTED">{intl.formatMessage({ id: 'task.status.rejected' })}</MenuItem>
      </Select>
    </StyledTaskStatus>
  );
};




const MUI_NAME = 'TaskStatus';
const StyledTaskStatus = styled('div', {
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

    '& .TaskStatus-progressBar': {
      width: '60%'
    },
    '& .TaskStatus-backgroundTrack': {
      width: '100%',
      height: theme.spacing(2),
      backgroundColor: '#eee',
      borderRadius: theme.spacing(3),
      overflow: 'hidden',
      boxShadow: 'inset 0 1px 3px rgba(0,0,0,0.2)',
    },
    '& .TaskStatus-progressDesc': {
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