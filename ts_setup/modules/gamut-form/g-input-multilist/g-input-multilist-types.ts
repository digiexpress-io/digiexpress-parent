import React from 'react'
import { OverridableStringUnion } from '@mui/types'

import { DialobApi } from '@dxs-ts/gamut-api';


export interface GInputMultilistClasses {
  root: string;
}
export type GInputMultilistClassKey = keyof GInputMultilistClasses;


// extension hook for adding custom input types
export interface GInputMultilistPropsVariantOverrides { };


export interface GInputMultilistProps {
  id: string;
  value: string[] | undefined;
  datasource: DialobApi.ActionValueSet;
  disabled: boolean;
  required: boolean;
  readOnly?: boolean;

  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: DialobApi.ControlLabelPosition,
  description: string | undefined;

  errors?: DialobApi.ActionError[] | undefined;
  keys?: boolean | undefined; // display keys

  /**
  - Styles resembling MUI Paper, which include a border, elevation, and padding/margins   
   */
  border?: boolean | undefined;

  variant: OverridableStringUnion<
    'multilist' | 'radio',
    GInputMultilistPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'multilist' | 'radio',
    GInputMultilistPropsVariantOverrides>,
    React.ElementType>; 

  component?: React.ElementType<GInputMultilistProps>;
}

