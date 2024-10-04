import React from 'react';
import { OverridableStringUnion } from '@mui/types';

import { GInputBase, LabelPosition } from '../g-input-base';
import { DialobApi } from '../api-dialob';
import { useThemeInfra, GInputTextAreaRoot } from './useThemeInfra';


// extension hook for adding custom input types
export interface GInputTextAreaPropsVariantOverrides { };

export interface GInputTextAreaProps {
  id: string;
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: LabelPosition,
  description: string | undefined;

  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;
  required?: boolean | undefined;
  rows?: number | undefined;


  variant: OverridableStringUnion<
    'textBox',
    GInputTextAreaPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'textBox',
    GInputTextAreaPropsVariantOverrides>,
    React.ElementType>; 

  component?: React.ElementType<GInputTextAreaProps>;
}

export const GInputTextArea: React.FC<GInputTextAreaProps> = (initProps) => {
  const { classes, slots, ownerState, props } = useThemeInfra(initProps);

  return (<GInputTextAreaRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputTextAreaRoot>);
}


