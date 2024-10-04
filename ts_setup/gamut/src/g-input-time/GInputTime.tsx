import 'react-time-picker/dist/TimePicker.css';
import 'react-clock/dist/Clock.css';

import React from 'react';
import ClearIcon from '@mui/icons-material/Clear';
import AccessTimeIcon from '@mui/icons-material/AccessTime';

import { useThemeProps } from '@mui/material';
import { OverridableStringUnion } from '@mui/types';
import TimePicker from 'react-time-picker';



import { GInputBase, GInputBaseProps, LabelPosition } from '../g-input-base';
import { GInputAdornment } from '../g-input-adornment';
import { DialobApi } from '../api-dialob';
import { GInputError } from '../g-input-error';
import { GInputLabel } from '../g-input-label';



import { InputHidden } from './InputHidden';
import { GInputTimeRoot, GInputTimeInput, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { useIntl } from 'react-intl';



// extension hook for adding custom input types
export interface GInputTimePropsVariantOverrides { };

export interface GInputTimeProps {
  id: string;
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: LabelPosition,
  description: string | undefined;
  format: string | undefined;

  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;

  variant: OverridableStringUnion<
    'time',
    GInputTimePropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'time',
    GInputTimePropsVariantOverrides>,
    React.ElementType>;

  component?: React.ElementType<GInputTimeProps>;
}



export const GInputTime: React.FC<GInputTimeProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { id, label, variant = 'time', labelPosition, errors } = props;
  const ownerState = { ...props, variant };
  const classes = useUtilityClasses(id, variant);

  const slots: GInputBaseProps<GInputTimeProps> = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      input: TimeAndCalendar,
      adornment: GInputAdornment
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState, name: id },
      label: { id, children: label ?? '', labelPosition },
      adornment: { id, children: props.description, title: label }
    }
  }

  return (<GInputTimeRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputTimeRoot>);
}



function parseInit(value: string | undefined) {
  if (value) {
    return value;
  }
  return null;
}


const TimeAndCalendar: React.FC<GInputTimeProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses(props.id, props.variant)
  const [value, setValue] = React.useState<string | null>(parseInit(props.value));

  const { format = 'HH:mm' } = props;

  const ownerState = {
    variant: props.variant ?? 'time',
  }

  return (
    <GInputTimeInput ownerState={ownerState} className={classes.input}>
      <InputHidden time={value} onChange={props.onChange} id={props.id} /> 

      <TimePicker 
        value={value} 
        onChange={(newValue) => setValue(newValue)} 
        format={format}
        
        clockIcon={<AccessTimeIcon />}
        clearIcon={<ClearIcon />}

        hourPlaceholder={intl.formatMessage({ id: 'gamut.forms.answer.date.placeholder.hour' })}
        minutePlaceholder={intl.formatMessage({ id: 'gamut.forms.answer.date.placeholder.minute' })}

        className='MuiInputBase-root'
      />

    </GInputTimeInput>

  );
}
