import React from 'react'
import { IconButton, Typography } from '@mui/material'
import RadioButtonCheckedIcon from '@mui/icons-material/RadioButtonChecked'
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked'

import { GInputSurveyQuestionBody, GInputSurveyQuestionLabel, useUtilityClasses } from './useUtilityClasses'
import { InputHidden } from './InputHidden'
import { GInputAdornment } from '../g-input-adornment'


export interface GInputSurveyQuestionProps {
  id: string;
  index: number;
  label: string | undefined;
  description: string | undefined;

  options: { id: string, label: string, description?: undefined | string }[];
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}



export const GInputSurveyQuestion: React.FC<GInputSurveyQuestionProps> = (props) => {

  const { id, value, onChange, description } = props;
  const classes = useUtilityClasses(id);
  const [internalValue, setInternalValue] = React.useState<string>(value ?? '');

  function handleOnClick(event: React.MouseEvent<HTMLButtonElement>, option: { id: string }) {
    setInternalValue(option.id);
  }
  const delegate: Omit<GInputSurveyQuestionProps, 'onChange'> = props;

  return (
    <>
      <GInputSurveyQuestionLabel className={classes.label} {...delegate}>
        <Typography>{props.label}</Typography>
        <GInputAdornment id={`${id}-label`} title={props.label} children={description} />
        <InputHidden id={id} choice={internalValue} onChange={onChange} />
      </GInputSurveyQuestionLabel>

      {props.options.map(e => (
        <GInputSurveyQuestionBody key={e.label} className={classes.body} {...delegate}>
          <IconButton onClick={(event) => handleOnClick(event, e)}>
            {e.id === value ? <RadioButtonCheckedIcon /> : <RadioButtonUncheckedIcon />}
          </IconButton>
        </GInputSurveyQuestionBody>
      ))}
    </>);
}