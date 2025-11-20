import React from 'react'
import { Box, Button, Divider, generateUtilityClass, Grid2, styled, Typography } from '@mui/material';
import { CheckCircleOutlineOutlined as CheckCircleOutlineOutlinedIcon } from '@mui/icons-material';
import { RadioButtonUncheckedOutlined as RadioButtonUncheckedOutlinedIcon } from '@mui/icons-material';
import composeClasses from '@mui/utils/composeClasses';

import { TaskApi } from '@dxs-ts/task-api';
import { useIntl } from 'react-intl';




export const TaskAssignmentReadOnly: React.FC<{ task: TaskApi.Task, onClickReview: (option: TaskApi.TaskCustomerAssignment) => void }> = ({ task, onClickReview }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  return (<>
    <StyledTaskAssignmentReadOnly className={classes.root}>
      {task.customerAssignments.map((option) => (
        <div key={option.id}>
          <Grid2 container alignItems="center" mb={1}>
            <Grid2 size={{ xs: 12, sm: 12, md: 6, lg: 6, xl: 6 }}>
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
            <Grid2 size={{ xs: 6, sm: 2, md: 2, lg: 2, xl: 2 }} display='flex' justifyContent='flex-end'>
              <Button disabled={option.status !== "COMPLETED"} onClick={() => onClickReview(option)}>{intl.formatMessage({ id: 'task.form.review' })}</Button>
            </Grid2>
          </Grid2>
          <Divider />
        </div>
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