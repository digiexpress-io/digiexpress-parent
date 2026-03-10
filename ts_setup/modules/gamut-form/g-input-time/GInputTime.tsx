import React from 'react';

import { useThemeProps } from '@mui/material';
import { OverridableStringUnion } from '@mui/types';


import { GInputBase, GInputBaseProps } from '../g-input-base';
import { GInputAdornment } from '../g-input-adornment';
import { DialobApi } from '@dxs-ts/gamut-api';
import { GInputError } from '../g-input-error';
import { GInputLabel } from '../g-input-label';

import { GInputTimeRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { TimeAndCalendar } from './TimeAndCalendar';



// extension hook for adding custom input types
export interface GInputTimePropsVariantOverrides { };

export interface GInputTimeProps {
  id: string;
  value: string | undefined;
  disabled: boolean;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: DialobApi.ControlLabelPosition,
  description: string | undefined;
  format: string | undefined;
  readOnly?: boolean;

  required: boolean;
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
      label: { id, children: label ?? '', labelPosition, required: props.required, errors: props.errors },
      adornment: { id, children: props.description, title: label, disabled: props.disabled }
    }
  }

  return (<GInputTimeRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputTimeRoot>);
}