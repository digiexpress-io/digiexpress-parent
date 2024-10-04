import React from 'react'
import { Typography } from '@mui/material'
import { useUtilityClasses } from './useUtilityClasses'
import { GInputSurveyOptionProps } from './GInputSurvey'



export const GInputSurveyOption: React.FC<GInputSurveyOptionProps> = (props) => {

  const { id } = props;
  const classes = useUtilityClasses(id);

  return (
    <div className={classes.option}>
      <Typography>{props.label}</Typography>
    </div>);
}