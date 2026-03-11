import React from 'react'
import { useThemeProps } from '@mui/material';


import { GInputBase } from '../g-input-base';
import { GInputBaseProps } from '../g-input-base';
import { GInputError } from '../g-input-error';
import { GInputLabel, GInputLabelProps } from '../g-input-label';
import { GInputAdornment } from '../g-input-adornment';


import { MUI_NAME, GInputMultilistRoot, useUtilityClasses } from './useUtilityClasses';
import { GInputMultilistProps } from './g-input-multilist-types';
import { CheckboxList, ReadOnlyCheckboxList } from './CheckboxList';
import { MultilistAutocomplete, ReadOnlyMultilist } from './MultilistAutocomplete';



export const GInputMultilist: React.FC<GInputMultilistProps> = (initProps) => {
  
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })

  const {
    variant = 'multilist',
    value = [],
    keys = false,
    id, 
    label, 
    labelPosition, errors
  } = props;

  const ownerState = {
    ...props,
    variant, value, keys
  }
  
  const InputComponent = (() => {
    switch (variant) {
      case 'radio': return props.readOnly ? ReadOnlyCheckboxList : CheckboxList;
      default: return props.readOnly ? ReadOnlyMultilist : MultilistAutocomplete;
    }
  })();

  const LabelComponent = variant === 'radio' ? Label : GInputLabel;

  const slots: GInputBaseProps<GInputMultilistProps> = {
    id,
    slots: {
      error: GInputError, 
      label: LabelComponent,
      adornment: GInputAdornment,
      input: InputComponent,
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState, name: id },
      label: { id, children: label ?? '', labelPosition, required: props.required, errors: props.errors },
      adornment: { id, children: props.description, title: label, disabled: props.disabled }
    }
  }
  const classes = useUtilityClasses(props.id, variant);

  return (<GInputMultilistRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputMultilistRoot>);
}

const Label: React.FC<GInputLabelProps> = (props) => {
  return (<GInputLabel {...props} braced />)
}



