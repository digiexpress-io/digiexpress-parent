import React from 'react'
import { Box, IconButton, Typography } from '@mui/material'
import { RadioButtonChecked as RadioButtonCheckedIcon } from '@mui/icons-material'
import { RadioButtonUnchecked as RadioButtonUncheckedIcon } from '@mui/icons-material'

import { GInputSurveyQuestionBody, GInputSurveyQuestionLabel, useUtilityClasses } from './useUtilityClasses'
import { InputHidden } from './InputHidden'
import { GInputAdornment } from '../g-input-adornment'
import { DialobApi } from '@dxs-ts/gamut-api'
import { GInputError } from '../g-input-error';


export interface GInputSurveyQuestionProps {
  id: string;
  index: number;
  label: string | undefined;
  description: string | undefined;

  options: { id: string, label: string, description?: undefined | string }[];
  disabled: boolean;
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  navref: React.MutableRefObject<any>;
  navrefid: string,
  errors?: DialobApi.ActionError[] | undefined;
  required: boolean;
}



export const GInputSurveyQuestion: React.FC<GInputSurveyQuestionProps> = (props) => {

  const { id, value, onChange, description, disabled, navref, navrefid, errors } = props;
  const classes = useUtilityClasses(id);
  const [internalValue, setInternalValue] = React.useState<string>(value ?? '');

  function handleOnClick(event: React.MouseEvent<HTMLButtonElement>, option: { id: string }) {
    setInternalValue(option.id);
  }
  const delegate: Omit<GInputSurveyQuestionProps, 'onChange'> = props;

  return (
    <>
      <GInputSurveyQuestionLabel className={classes.label} {...delegate}>

        <Box display='flex' flexDirection='column'>
          <GInputError errors={errors} id={id} />

          <Box display='flex' flexDirection='row' justifyContent='flex-end'>
            <Typography ref={navref} id={navrefid} color={props.errors?.length ? 'error' : 'inherit'}>{props.label}</Typography>
            {props.required && (
              <Box display='flex' alignItems='center'>
                <Box ml={0.5}><Typography fontSize='15pt' fontWeight='bold' color='error.main'>*</Typography></Box>
              </Box>
            )}
          </Box>
        </Box>
        <GInputAdornment id={`${id}-label`} title={props.label} children={description} disabled={props.disabled} />
        <InputHidden id={id} choice={internalValue} onChange={onChange} />
      </GInputSurveyQuestionLabel>


      {props.options.map(e => (
        <GInputSurveyQuestionBody key={e.label} className={classes.body} {...delegate}>
          <IconButton disabled={disabled} onClick={(event) => handleOnClick(event, e)}>
            {e.id === value ? <RadioButtonCheckedIcon /> : <RadioButtonUncheckedIcon />}
          </IconButton>
        </GInputSurveyQuestionBody>
      ))}
    </>);
}