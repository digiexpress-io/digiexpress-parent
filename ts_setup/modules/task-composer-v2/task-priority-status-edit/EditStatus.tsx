import React from 'react';
import { Box, generateUtilityClass, MenuItem, Select, SelectChangeEvent, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { TaskApi } from '@dxs-ts/task-api';

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


const TaskProgressBar: React.FC<{ status: TaskApi.TaskStatus }> = ({ status }) => {
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
  const color = getStatusColor(status ?? TaskApi.TaskStatus.NEW);


  return (
    <Box className={classes.progressBar}>
      <Typography sx={{ fontWeight: 500, mb: 1 }}>
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


export const EditStatus: React.FC<{ onChange: (e: SelectChangeEvent) => void, status: TaskApi.TaskStatus }> = ({ onChange, status }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <StyledEditStatus className={classes.root}>
      <TaskProgressBar status={status} />

      <Select
        value={status}
        onChange={onChange}
        size="small"
        sx={{
          width: '30%',
          alignSelf: 'center',
        }}
      >
        {Object.entries(TaskApi.task_status_messages).map(([key, message]) => (
          <MenuItem key={key} value={key}>
            <Box display="flex" alignItems="center">
              <Box sx={{
                width: 10,
                height: 10,
                borderRadius: '50%',
                backgroundColor: getStatusColor(key as TaskApi.TaskStatus),
                mr: 1,
              }}
              />
              {intl.formatMessage(message)}
            </Box>
          </MenuItem>
        ))}

      </Select>
    </StyledEditStatus>
  );
};

const MUI_NAME = 'EditStatus';
const StyledEditStatus = styled('div', {
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

    '& .EditStatus-progressBar': {
      width: '60%'
    },
    '& .EditStatus-backgroundTrack': {
      width: '100%',
      height: theme.spacing(2),
      backgroundColor: '#eee',
      borderRadius: theme.spacing(3),
      overflow: 'hidden',
      boxShadow: 'inset 0 1px 3px rgba(0,0,0,0.2)',
    },
    '& .EditStatus-progressDesc': {
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