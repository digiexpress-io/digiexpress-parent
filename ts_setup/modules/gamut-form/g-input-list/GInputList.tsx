import React from 'react'
import { useThemeProps } from '@mui/material';

import { GInputBase, GInputBaseProps } from '../g-input-base';
import { GInputError } from '../g-input-error';
import { GInputLabel } from '../g-input-label';
import { GInputAdornment } from '../g-input-adornment';


import { GInputListRoot, useUtilityClasses, MUI_NAME } from './useUtilityClasses';
import { GInputRadio } from './GInputRadio';
import { GInputListProps } from './g-input-list-types';
import { GInputDropdown } from './GInputDropdown';
import { GInputAutoComplete } from '../g-input-autocomplete';


export const GInputList: React.FC<GInputListProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })

  const {
    variant = 'list',
    keys = false,
    id, label, labelPosition, errors
  } = props;


  const ownerState = { ...props, variant, keys, name: id }
  //const InputComponent = variant === 'list-radio' ? GInputRadio : GInputDropdown;

  const InputComponent = (() => {
    switch (variant) {
      case 'list-radio':
        return GInputRadio;
      case 'autocomplete':
        return GInputListAutocomplete;
      case 'list':
      default:
        return GInputDropdown;
    }
  })();

  const slots: GInputBaseProps<GInputListProps> = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      adornment: GInputAdornment,
      input: InputComponent,
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState },
      label: { id, children: label ?? '', labelPosition, required: props.required, errors: props.errors },
      adornment: { id, children: props.description, title: label, disabled: props.disabled }
    }
  }

  const classes = useUtilityClasses(props.id, variant);
  return (<GInputListRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputListRoot>);
}



const GInputListAutocomplete: React.FC<GInputListProps> = (initProps) => {
  return <GInputAutoComplete
    disabled={initProps.disabled}
    id={initProps.id}
    datasource={initProps.datasource}
    multiple={false}
    onChange={initProps.onChange}
    value={initProps.value}
  />
}