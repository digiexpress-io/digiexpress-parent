import React from 'react';
import { Box, generateUtilityClass, MenuItem, Select, SelectChangeEvent, styled, Typography } from '@mui/material';
import { TaskCardStyleDefinition } from './cardThemeConfig';
import composeClasses from '@mui/utils/composeClasses';

type TaskStatus = 'NEW' | 'OPEN' | 'COMPLETED' | 'REJECTED';


const TaskProgressBar: React.FC<{ status: TaskStatus, style: TaskCardStyleDefinition }> = ({ status, style }) => {
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
        return '#9575cd'; // Purpley
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
      <Typography sx={{ ...style.bodyTypography, fontWeight: 500, mb: 1 }}>Status: {status}</Typography>
      <Box className={classes.backgroundTrack}>
        <Box sx={{
          height: '100%',
          width: `${progress}%`,
          backgroundColor: color,
          transition: 'width 0.3s ease-in-out',
        }}
        />
      </Box>
      <Typography style={{ marginTop: 4, textAlign: 'right', fontSize: 12 }}>{progress}% complete</Typography>
    </Box>
  );
};

export const EveliTaskStatus: React.FC<{ style: TaskCardStyleDefinition }> = ({ style }) => {
  const classes = useUtilityClasses();
  const [status, setStatus] = React.useState<TaskStatus>('NEW');

  const handleStatusChange = (e: SelectChangeEvent) => {
    setStatus(e.target.value as TaskStatus);
  };

  return (
    <TaskStatus className={classes.root}>
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
        <MenuItem value="NEW">NEW</MenuItem>
        <MenuItem value="OPEN">OPEN</MenuItem>
        <MenuItem value="COMPLETED">COMPLETED</MenuItem>
        <MenuItem value="REJECTED">REJECTED</MenuItem>
      </Select>
    </TaskStatus>
  );
};




const MUI_NAME = 'TaskStatus';
const TaskStatus = styled('div', {
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
    }
  };
})


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    progressBar: ['progressBar'],
    backgroundTrack: ['backgroundTrack'],
    progressIndicator: ['progressIndicator']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}