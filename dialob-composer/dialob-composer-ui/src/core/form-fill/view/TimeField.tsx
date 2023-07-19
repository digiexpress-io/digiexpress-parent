import React from 'react';

import {format, parse} from 'date-fns';

import TextField from '@mui/material/TextField';
import TimePicker from '@mui/lab/TimePicker';

import { DescriptionWrapper, RenderErrors } from '@dialob/fill-material';
import { ItemAction, SessionError } from '@dialob/fill-api';
import { useFillActions } from '@dialob/fill-react';



const timeFormat = 'HH:mm';

export interface TimeFieldProps {
  timefield: ItemAction<'time'>['item'];
  errors: SessionError[];
};

const formatToWire = (value: any): string => {
  try {
    return format(value, timeFormat);
  } catch (error) {
    return '';
  }
}

export const TimeField: React.FC<TimeFieldProps> = ({ timefield, errors }) => {
  const { setAnswer } = useFillActions();
  const handleChange = (value: any) => {
    setAnswer(timefield.id, formatToWire(value));
  }
  const value = timefield.value ? timefield.value as string : null;
  return (
    <DescriptionWrapper text={timefield.description} title={timefield.label}>
      <TimePicker
        ampm={false}
        label={timefield.label}
        value={value ? parse(value, timeFormat, new Date()) : null}
        onChange={handleChange}
        renderInput={(props: any) => <TextField {...props}
          fullWidth={true}
          margin='normal'
          required={timefield.required}
          error={errors.length > 0}
          helperText={<RenderErrors errors={errors} />}
        />}
      />
    </DescriptionWrapper>
  );
}

