import React from 'react';
import { Button, ButtonGroup, generateUtilityClass, Stack, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { TaskApi } from '@dxs-ts/task-api';

export const EditPriority: React.FC<{
  priority: TaskApi.TaskPriority;
  onChange: (priority: TaskApi.TaskPriority) => void;
}> = ({ priority, onChange }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <StyledEditPriority className={classes.root}>
      <Stack direction='column' width='100%'>
        <Typography fontWeight={500} mb={1} sx={{ fontWeight: 500 }}>
          {intl.formatMessage({ id: 'task.priority', defaultMessage: 'Priority' })}
          {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
          {priority}
        </Typography>
        <ButtonGroup fullWidth className={classes.prioritySelect} disableElevation>
          {Object.entries(TaskApi.task_priority_messages).map(([key, message]) => {
            const level = key as TaskApi.TaskPriority;
            const color = TaskApi.task_priority_hex[level];
            const isActive = priority === level;
            return (
              <Button
                key={level}
                variant={isActive ? 'contained' : 'outlined'}
                onClick={() => onChange(level)}
                sx={{
                  backgroundColor: isActive ? color : 'transparent',
                  borderColor: isActive ? color : '#ccc',
                  color: isActive ? '#FFFFFF' : 'rgba(0, 0, 0, 0.38)',

                  '&:hover': {
                    backgroundColor: isActive ? color : 'rgba(0, 0, 0, 0.04)',
                    borderColor: isActive ? color : '#ccc',
                  },
                }}
              >
                {intl.formatMessage(message)}
              </Button>
            );
          })}
        </ButtonGroup>
      </Stack>
    </StyledEditPriority>
  );
};



const MUI_NAME = 'EditPriority';
const StyledEditPriority = styled('div', {
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
    alignItems: "center",
    '& .EditPriority-prioritySelect': {
      '& .MuiButton-root': {
        textTransform: 'none',
        fontWeight: 500,
        borderColor: theme.palette.divider,
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
