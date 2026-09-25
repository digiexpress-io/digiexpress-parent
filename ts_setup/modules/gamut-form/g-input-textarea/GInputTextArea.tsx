import React from 'react';
import { OverridableStringUnion } from '@mui/types';
import { Typography } from '@mui/material';

import { GInputBase } from '../g-input-base';
import { DialobApi } from '@dxs-ts/gamut-api';
import { useThemeInfra, GInputTextAreaRoot } from './useUtilityClasses';


export interface GInputTextAreaPropsVariantOverrides { };

export interface GInputTextAreaProps {
  id: string;
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: DialobApi.ControlLabelPosition,
  description: string | undefined;
  disabled: boolean;
  readOnly?: boolean;

  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;
  required: boolean;
  rows?: number | undefined;
  charLimit?: number | undefined;


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
  const { classes, slots, ownerState, props, isCharLimitVisible, charLimit, currentLength } = useThemeInfra(initProps);

  return (
    <GInputTextAreaRoot className={classes.root} ownerState={ownerState} as={props.component}>
      <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
      {isCharLimitVisible && (
        <Typography variant="caption" className={classes.charCount}>
          {currentLength} / {charLimit}
        </Typography>
      )}
    </GInputTextAreaRoot>
  );
}


