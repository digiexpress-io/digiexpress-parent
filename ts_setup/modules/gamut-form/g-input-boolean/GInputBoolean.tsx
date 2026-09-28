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


// extension hook for adding custom input types
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
    'checkbox',
    GInputBooleanPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'checkbox',
    GInputBooleanPropsVariantOverrides>,
    React.ElementType>; 

  readOnly?: boolean;
  singleCheckbox?: boolean;

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
      input: resolveInputComponent(props.singleCheckbox, props.readOnly),
      adornment: GInputAdornment
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState, name: id },
      label: { id, children: label ?? '', labelPosition, required: props.required, errors: props.errors },
      adornment: { id, children: props.description, title: label, disabled: props.disabled }
    }
  }

  return (<GInputBooleanRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputBooleanRoot>);
}


function resolveInputComponent(isSingle: boolean | undefined, isReadOnly: boolean | undefined) {
  if (isSingle) {
    return isReadOnly ? ReadOnlySingleCheckbox : SingleCheckbox;
  }
  return isReadOnly ? ReadOnlyYesAndNoCheckbox : YesAndNoCheckbox;
}




