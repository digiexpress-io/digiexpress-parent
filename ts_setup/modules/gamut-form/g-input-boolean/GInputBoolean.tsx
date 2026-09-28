import React from 'react';
import { useThemeProps } from '@mui/material';
import { OverridableStringUnion } from '@mui/types';

import { GInputBase, GInputBaseProps } from '../g-input-base';
import { DialobApi } from '@dxs-ts/gamut-api';
import { GInputError } from '../g-input-error';
import { GInputLabel } from '../g-input-label';
import { GInputAdornment } from '../g-input-adornment';

import { useUtilityClasses, MUI_NAME, GInputBooleanRoot } from './useUtilityClasses';
import { ReadOnlyYesAndNoCheckbox } from './ReadOnlyYesAndNoCheckbox';
import { YesAndNoCheckbox } from './YesAndNoCheckBox';
import { SingleCheckbox, ReadOnlySingleCheckbox } from './SingleCheckbox';


export interface GInputBooleanPropsVariantOverrides { };

export interface GInputBooleanProps {
  id: string;
  value: boolean | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: DialobApi.ControlLabelPosition,
  description: string | undefined;
  disabled: boolean;

  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;
  required: boolean;

  variant: OverridableStringUnion<
    'checkbox' | 'singleCheckbox',
    GInputBooleanPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'checkbox',
    GInputBooleanPropsVariantOverrides>,
    React.ElementType>;

  readOnly?: boolean;

  component?: React.ElementType<GInputBooleanProps>;
}



export const GInputBoolean: React.FC<GInputBooleanProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { id, label, variant = 'checkbox', labelPosition, errors } = props;
  const ownerState = { ...props, variant };
  const classes = useUtilityClasses(id, variant);

  const slots: GInputBaseProps<GInputBooleanProps> =  {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      input: resolveInputComponent(variant, props.readOnly),
      adornment: GInputAdornment
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState, name: id },
      label: { id, children: label ?? '', labelPosition: variant === 'singleCheckbox' ? 'label-left' : labelPosition, required: props.required, errors: props.errors },
      adornment: { id, children: props.description, title: label, disabled: props.disabled }
    }
  }

  return (<GInputBooleanRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputBooleanRoot>);
}


function resolveInputComponent(variant: string, isReadOnly: boolean | undefined) {
  if (variant === 'singleCheckbox') {
    return isReadOnly ? ReadOnlySingleCheckbox : SingleCheckbox;
  }
  return isReadOnly ? ReadOnlyYesAndNoCheckbox : YesAndNoCheckbox;
}




