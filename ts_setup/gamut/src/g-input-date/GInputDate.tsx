import React from 'react';

import { useThemeProps } from '@mui/material';
import { OverridableStringUnion } from '@mui/types';
import EditCalendarIcon from '@mui/icons-material/EditCalendar';
import ClearIcon from '@mui/icons-material/Clear';

import 'react-date-picker/dist/DatePicker.css';
import 'react-calendar/dist/Calendar.css';

import DatePicker from 'react-date-picker';
import { DateTime } from 'luxon';

import { GInputBase, GInputBaseProps, LabelPosition } from '../g-input-base';
import { DialobApi } from '../api-dialob';
import { GInputError } from '../g-input-error';
import { GInputLabel } from '../g-input-label';
import { GInputAdornment } from '../g-input-adornment';

import { InputHidden } from './InputHidden';

import { GInputDateRoot, GInputDateInput, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { useIntl } from 'react-intl';



// extension hook for adding custom input types
export interface GInputDatePropsVariantOverrides { };

export interface GInputDateProps {
  id: string;
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: LabelPosition;
  description: string | undefined;
  format: string | undefined;

  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;

  variant: OverridableStringUnion<
    'date',
    GInputDatePropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'date',
    GInputDatePropsVariantOverrides>,
    React.ElementType>;

  component?: React.ElementType<GInputDateProps>;
}



export const GInputDate: React.FC<GInputDateProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { id, label, variant = 'date', labelPosition, errors } = props;
  const ownerState = { ...props, variant };
  const classes = useUtilityClasses(id, variant);

  const slots: GInputBaseProps<GInputDateProps> = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      input: DateAndCalendar,
      adornment: GInputAdornment
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState, name: id },
      label: { id, children: label ?? '', labelPosition },
      adornment: { id, children: props.description, title: label }
    }
  }

  return (<GInputDateRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputDateRoot>);
}



function parseInit(value: string | undefined) {
  if (value) {
    const result = DateTime.fromISO(value);
    return result.isValid ? result : null; 
  }
  return null;
}


const DateAndCalendar: React.FC<GInputDateProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses(props.id, props.variant)
  const [value, setValue] = React.useState<DateTime | null>(parseInit(props.value));
  
  const { format = 'dd.MM.yyyy' } = props;

  const ownerState = {
    variant: props.variant ?? 'date',
  }

  return (
    <GInputDateInput ownerState={ownerState} className={classes.input}>
      
      <InputHidden dateTime={value} onChange={props.onChange} id={props.id} /> 

      <DatePicker 
        onChange={(newValue) => setValue(newValue ? DateTime.fromJSDate(newValue as Date) : null)}
        value={value?.toJSDate()} 

        calendarIcon={<EditCalendarIcon />}
        clearIcon={<ClearIcon />}

        format={format}

        className='MuiInputBase-root'
        calendarProps={{

        }}

        dayPlaceholder={intl.formatMessage({ id: 'gamut.forms.answer.date.placeholder.day' })}
        monthPlaceholder={intl.formatMessage({ id: 'gamut.forms.answer.date.placeholder.month' })}
        yearPlaceholder={intl.formatMessage({ id: 'gamut.forms.answer.date.placeholder.year' })}
      />

    </GInputDateInput>

  );
}
