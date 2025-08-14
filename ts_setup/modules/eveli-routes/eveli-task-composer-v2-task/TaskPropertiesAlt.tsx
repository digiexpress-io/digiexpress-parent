import React from 'react';
import { useTaskDashboard } from '../eveli-task-composer-v2';
import { Box, styled } from '@mui/system';
import { generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';



export const TaskPropertiesAlt: React.FC = () => {
  const { task } = useTaskDashboard();
  const classes = useUtilityClasses();

  return (
    <StyledTaskPropertiesAlt className={classes.taskAltCard}>
     <Box></Box>
    </StyledTaskPropertiesAlt>)
}


const MUI_NAME = 'TaskPropertiesAlt';
const StyledTaskPropertiesAlt = styled(Box, {
  name: MUI_NAME,
  slot: 'taskAltCard',
  overridesResolver: (_props, styles) => {
    return [
      styles.taskAlt,
    ];
  },
})(({ theme }) => {

  return {
    
  }
});


export const useUtilityClasses = () => {
  const slots = {
    taskAltCard: ['taskAltCard'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
