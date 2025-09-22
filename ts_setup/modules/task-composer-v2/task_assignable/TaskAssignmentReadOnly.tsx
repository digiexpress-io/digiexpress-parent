import React from 'react'
import { Box, Divider, generateUtilityClass, Grid2, styled, Typography } from '@mui/material';
import CheckCircleOutlineOutlinedIcon from '@mui/icons-material/CheckCircleOutlineOutlined';
import RadioButtonUncheckedOutlinedIcon from '@mui/icons-material/RadioButtonUncheckedOutlined';
import composeClasses from '@mui/utils/composeClasses';



interface AssignedForm {
  name: string,
  status: string
}

const options: AssignedForm[] = [
  { name: 'Application for inspection', status: 'COMPLETE' },
  { name: 'Building permit', status: 'OPEN' },
  { name: 'Release and user agreement', status: 'OPEN' }
];

export const TaskAssignmentReadOnly: React.FC = () => {
  const classes = useUtilityClasses();

  return (
    <StyledTaskAssignmentReadOnly className={classes.root}>
      {options.map((option) => (<>
        <Grid2 key={option.name} container alignItems="center" mb={1} 
        sx={option.status === 'COMPLETE' ? {
        
        } : undefined}>
          <Grid2 size={{ xs: 12, sm: 12, md: 7, lg: 7, xl: 7 }}>
            <Box display="flex" alignItems="center" gap={1}>
              {option.status === "COMPLETE" ? (
                <CheckCircleOutlineOutlinedIcon color="success" />
              ) : (
                <RadioButtonUncheckedOutlinedIcon color="disabled" />
              )}
              <Typography fontWeight={500}>{option.name}</Typography>
            </Box>
          </Grid2>
          <Grid2 size={{ xs: 12, sm: 12, md: 5, lg: 5, xl: 5 }}>
            <Typography>{option.status}</Typography>
          </Grid2>
        </Grid2>
        <Divider />
      </>
      ))}
    </StyledTaskAssignmentReadOnly>
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
  },

}));

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};