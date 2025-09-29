import React from 'react';

import { useThemeProps } from '@mui/material';
import { OverridableStringUnion } from '@mui/types';

import { GInputBase, GInputBaseProps } from '../g-input-base';
import { DialobApi } from '@dxs-ts/gamut-api';
import { GInputError } from '../g-input-error';
import { GInputLabel } from '../g-input-label';
import { GInputAdornment } from '../g-input-adornment';


import { GInputDateRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { DateAndCalendar } from './DateAndCalendar';



// extension hook for adding custom input types
export interface GInputDatePropsVariantOverrides { };

export interface GInputDateProps {
  id: string;
  disabled: boolean;
  value: string | undefined;
  label: string | undefined;
  labelPosition: DialobApi.ControlLabelPosition;
  description: string | undefined;
  format: string | undefined;
  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;
  required: boolean;

  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  setExtendedErrors?: (extendedErrors: DialobApi.ActionError[]) => void;

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
  const [extendedErrors, setExtendedErrors] = React.useState<DialobApi.ActionError[]>([])
  const handleExtendedErrors = React.useCallback((nextErrors: DialobApi.ActionError[] | undefined) => {
    setExtendedErrors(prevErrors => {
      const s1 = JSON.stringify(nextErrors ?? []);
      const s2 = JSON.stringify(prevErrors ?? []);
      return s1 === s2 ? prevErrors : (nextErrors ?? []);
    });

  }, [setExtendedErrors])

  const errors: DialobApi.ActionError[] = [...extendedErrors, ...(props.errors ?? [])]

  const { id, label, variant = 'date', labelPosition } = props;
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
      input: { ...ownerState, name: id, setExtendedErrors: handleExtendedErrors },
      label: { id, children: label ?? '', labelPosition, required: props.required },
      adornment: { id, children: props.description, title: label, disabled: props.disabled }
    }
  }

  return (<GInputDateRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputDateRoot>);
}