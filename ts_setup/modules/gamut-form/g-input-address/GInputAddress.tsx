import React from 'react'

import { OverridableStringUnion } from '@mui/types'
import { useThemeProps } from '@mui/material'

import { MapProvider } from '@dxs-ts/gamut-api'
import { DialobApi } from '@dxs-ts/gamut-api'
import { GInputError } from '../g-input-error'
import { GInputLabel } from '../g-input-label'
import { GInputAdornment } from '../g-input-adornment'
import { GInputBase, GInputBaseProps } from '../g-input-base'

import { MUI_NAME, GInputAddressRoot, useUtilityClasses } from './useUtilityClasses'
import { useIntl } from 'react-intl'
import { InputAddress, ReadOnlyAddress } from './InputAddress'
import { InputMap } from './InputMap'
import { InputProvider } from './InputProvider'


// extension hook for adding custom input types
export interface GInputAddressPropsVariantOverrides { }

export interface GInputAddressProps {
  id: string;
  labelPosition: DialobApi.ControlLabelPosition,
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  description: string | undefined;
  disabled: boolean;

  options?: {
    defaultValue?: { lat: number, lng: number } | undefined;
    defaultCountryCodes?: string | undefined;
    mapHeight?: string | undefined;
  };

  readOnly?: boolean;

  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;
  required: boolean;


  variant: OverridableStringUnion<
    'address',
    GInputAddressPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'address',
    GInputAddressPropsVariantOverrides>,
    React.ElementType>;

  component?: React.ElementType<GInputAddressProps>;
}


export const GInputAddress: React.FC<GInputAddressProps> = (initProps) => {
  const intl = useIntl();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const {
    variant = 'address',
    options = {},
    labelPosition
  } = props;

  const {
    // somewhere in Helsinki
    defaultValue = {
      lat: 60.2,
      lng: 25.0
    },
    defaultCountryCodes = 'FI',
    mapHeight = '500px'
  } = options;


  const classes = useUtilityClasses(props.id, variant);
  const ownerState = { ...props, variant, defaultValue };

  const { id, label, description, errors } = props;
  const slots: GInputBaseProps<GInputAddressProps> = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      adornment: GInputAdornment,
      input: props.readOnly ? ReadOnlyAddress : InputAddress,
      secondary: InputMap
    },
    slotProps: {
      error: { id, errors },
      input: { name: id, ...props, options: { ...options, defaultValue, defaultCountryCodes, mapHeight } },
      label: { id, children: label ?? '', labelPosition, required: props.required, errors: props.errors },
      adornment: { id, children: description, title: label ?? '', disabled: props.disabled },
      secondary: { name: id, ...props, options: { ...options, defaultValue, defaultCountryCodes, mapHeight } }
    }
  }

  return (
    <MapProvider options={{ countrycodes: defaultCountryCodes, locale: intl.locale }}>
      <InputProvider id={id} onChange={props.onChange} value={props.value}>
        <GInputAddressRoot className={classes.root} ownerState={ownerState} as={props.component}>
          <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
        </GInputAddressRoot>
      </InputProvider>
    </MapProvider>);
}
