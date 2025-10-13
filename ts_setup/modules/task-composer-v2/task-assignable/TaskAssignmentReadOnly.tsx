import React from 'react'
import { Box, Divider, generateUtilityClass, Grid2, styled, Typography } from '@mui/material';
import { CheckCircleOutlineOutlined as CheckCircleOutlineOutlinedIcon } from '@mui/icons-material';
import { RadioButtonUncheckedOutlined as RadioButtonUncheckedOutlinedIcon } from '@mui/icons-material';
import composeClasses from '@mui/utils/composeClasses';

import { TaskApi } from '@dxs-ts/task-api';




export const TaskAssignmentReadOnly: React.FC<{ task: TaskApi.Task }> = ({ task }) => {
  const classes = useUtilityClasses();

  return (<>
    <StyledTaskAssignmentReadOnly className={classes.root}>
      {task.customerAssignments.map((option) => (
        <>
          <Grid2 key={option.id} container alignItems="center" mb={1}>
            <Grid2 size={{ xs: 12, sm: 12, md: 8, lg: 8, xl: 8 }}>
              <Box display="flex" alignItems="center" gap={1}>
                {option.status === "COMPLETED" ? (
                  <CheckCircleOutlineOutlinedIcon color="success" />
                ) : (
                  <RadioButtonUncheckedOutlinedIcon color="disabled" />
                )}
                <Typography fontWeight={500}>{option.description}</Typography>
              </Box>
            </Grid2>
            <Grid2 size={{ xs: 6, sm: 6, md: 2, lg: 2, xl: 2 }}>
              <Typography>{option.locale}</Typography>
            </Grid2>
            <Grid2 size={{ xs: 6, sm: 2, md: 2, lg: 2, xl: 2 }}>
              <Typography>{option.status}</Typography>
            </Grid2>
          </Grid2>
          <Divider />
        </>
      ))}
    </StyledTaskAssignmentReadOnly>
  </>
  )
}





const MUI_NAME = 'TaskAssignmentReadOnly';
const StyledTaskAssignmentReadOnly = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => ({

  '.MuiDivider-root': {
    marginTop: theme.spacing(0.5),
    marginBottom: theme.spacing(0.5)
  }

}));

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};