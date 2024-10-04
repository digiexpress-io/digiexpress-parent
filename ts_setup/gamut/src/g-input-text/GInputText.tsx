import React from 'react'

import { OverridableStringUnion } from '@mui/types'
import { TextField, useThemeProps } from '@mui/material'

import { DialobApi } from '../api-dialob'
import { GInputError } from '../g-input-error'
import { GInputLabel } from '../g-input-label'
import { GInputAdornment } from '../g-input-adornment'
import { GInputBase, GInputBaseAnyProps, GInputBaseProps, LabelPosition } from '../g-input-base'

import { MUI_NAME, GInputTextRoot, useUtilityClasses } from './useUtilityClasses'


// extension hook for adding custom input types
export interface GInputTextPropsVariantOverrides { }

export interface GInputTextProps {
  id: string;
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: LabelPosition,
  description: string | undefined;

  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;
  required?: boolean | undefined;


  variant: OverridableStringUnion<
    'text',
    GInputTextPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'text',
    GInputTextPropsVariantOverrides>,
    React.ElementType>; 

  component?: React.ElementType<GInputTextProps>;
}


export const GInputText: React.FC<GInputTextProps> = (initProps) => {
  
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { variant = 'text', labelPosition, errors } = props;
  const classes = useUtilityClasses(props.id, variant);
  const ownerState = { ...props, variant };

  const { id, label, description } = props;
  const slots: GInputBaseProps<GInputTextProps> = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      adornment: GInputAdornment,
      input: TextInput,
    },
    slotProps: {
      error: { id, errors },
      input: { name: id, ...props },
      label: { id, children: label ?? '', labelPosition },
      adornment: { id, children: description, title: label ?? '' }
    }
  }



  return (<GInputTextRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputTextRoot>);
}


const TextInput: React.FC<GInputBaseAnyProps & GInputTextProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  return (<TextField value={props.value} name={props.name} onChange={props.onChange} className={classes.input} error={(props.errors?.length ?? 0) > 0} />)
}