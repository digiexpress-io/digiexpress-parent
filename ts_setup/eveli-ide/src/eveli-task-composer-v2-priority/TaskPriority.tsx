import React, { useState } from 'react';
import { Button, ButtonGroup, generateUtilityClass, Stack, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { TaskCardStyleDefinition } from '../eveli-task-composer-v2-task-card';
import { useIntl } from 'react-intl';

type Priority = 'LOW' | 'NORMAL' | 'HIGH';

export const TaskPriority: React.FC<{ style: TaskCardStyleDefinition }> = ({ style }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const [priority, setPriority] = useState<Priority>('LOW');

  const priorities: { level: Priority, intl: string, color: string }[] = [
    { level: 'LOW', intl: 'task.priority.low', color: '#45a048' },
    { level: 'NORMAL', intl: 'task.priority.normal', color: '#2196f3' },
    { level: 'HIGH', intl: 'task.priority.high', color: '#f44336' },
  ];

  return (
    <StyledTaskPriority className={classes.root} style={style}>
      <Stack direction='column' width='100%'>
        <Typography fontWeight={500} mb={1} sx={{ ...style.bodyTypography, fontWeight: 500 }}>
          {intl.formatMessage({ id: 'task.priority', defaultMessage: 'Priority' })}
          {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
          {priority}
        </Typography>
        <ButtonGroup fullWidth className={classes.prioritySelect} disableElevation>
          {priorities.map(({ level, color, intl: intlId }) => {
            const isActive = priority === level;
            return (
              <Button key={level} variant={isActive ? 'contained' : 'outlined'} onClick={() => setPriority(level)}
                sx={{
                  backgroundColor: isActive ? color : 'transparent',
                  borderColor: isActive ? color : '#ccc',
                  color: isActive ? '#FFFFFF' : 'rgba(0, 0, 0, 0.38)',
                  ...style.bodyTypographySmall,
                  '&:hover': {
                    backgroundColor: isActive ? color : 'rgba(0, 0, 0, 0.04)',
                    borderColor: isActive ? color : '#ccc',
                  },
                }}
              >
                {intl.formatMessage({ id: intlId })}
              </Button>
            );
          })}
        </ButtonGroup>
      </Stack>
    </StyledTaskPriority>
  );
};



const MUI_NAME = 'TaskPriority';
const StyledTaskPriority = styled('div', {
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
    '& .TaskPriority-prioritySelect': {
      '& .MuiButton-root': {
        textTransform: 'none',
        fontWeight: 500,
        borderColor: '#ccc',
        width: '100%',

        padding: theme.spacing(0.5, 2),   
        minHeight: 32,                   
      },
      '& .MuiButton-root:first-of-type': {
        borderTopLeftRadius: theme.spacing(1),
        borderBottomLeftRadius: theme.spacing(1),
      },
      '& .MuiButton-root:last-of-type': {
        borderTopRightRadius: theme.spacing(1),
        borderBottomRightRadius: theme.spacing(1),
      }
    }

  };
})


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    prioritySelect: ['prioritySelect']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
