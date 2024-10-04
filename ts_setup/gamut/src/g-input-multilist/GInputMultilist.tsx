import React from 'react'
import { useThemeProps, Typography, Button } from '@mui/material';
import { OverridableStringUnion } from '@mui/types'
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';

import { DialobApi } from '../api-dialob';
import { GInputBase, LabelPosition } from '../g-input-base';
import { GInputBaseProps } from '../g-input-base';
import { GInputError } from '../g-input-error';
import { GInputLabel, GInputLabelProps } from '../g-input-label';
import { GInputAdornment } from '../g-input-adornment';
import { GInputBaseAnyProps } from '../g-input-base';

import { MUI_NAME, GInputMultilistRoot, useUtilityClasses, GInput } from './useUtilityClasses';



// extension hook for adding custom input types
export interface GInputMultilistPropsVariantOverrides { };


export interface GInputMultilistProps {
  id: string;
  value: string[] | undefined;
  datasource: DialobApi.ActionValueSet;

  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: LabelPosition,
  description: string | undefined;

  errors?: DialobApi.ActionError[] | undefined;
  keys?: boolean | undefined; // display keys

  variant: OverridableStringUnion<
    'multilist',
    GInputMultilistPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'multilist',
    GInputMultilistPropsVariantOverrides>,
    React.ElementType>; 

  component?: React.ElementType<GInputMultilistProps>;
}

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
  
  const slots: GInputBaseProps<GInputMultilistProps> = {
    id,
    slots: {
      error: GInputError, 
      label: Label,
      adornment: GInputAdornment,
      input: CheckboxList,
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState, name: id },
      label: { id, children: label ?? '', labelPosition },
      adornment: { id, children: props.description, title: label }
    }
  }
  const classes = useUtilityClasses(props.id, variant);

  return (<GInputMultilistRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputMultilistRoot>);
}



const Label: React.FC<GInputLabelProps> = (props) => {
  return (<GInputLabel {...props} braced/>)
}


const Checkbox: React.FC<{
  optionKey: string,
  optionValue: string,
  ownerState: GInputBaseAnyProps & GInputMultilistProps,
}> = (props) => {
  const ref = React.useRef<HTMLInputElement>(null); 

  
  const {ownerState, optionKey, optionValue} = props;
  const { onChange, value, keys, variant, id } = ownerState;
  const checked = value?.includes(optionKey) ?? false;
  const classes = useUtilityClasses(id, variant);


  React.useEffect(() => {
    function poulateTheChange(event: any) {
      onChange(event);
    }
    ref.current?.addEventListener("input", poulateTheChange);
    return () => ref.current?.removeEventListener("input", poulateTheChange);
  }, [onChange]);


  function toggleInput() {
    const event = new Event('input', { bubbles: true });
    ref.current?.dispatchEvent(event);
  }

  function doNothing() {
  }

  return (
    <Button className={classes.option} variant='outlined'
      onClick={toggleInput}
      startIcon={checked ? <CheckBoxIcon className={classes.optionIcon} /> : <CheckBoxOutlineBlankIcon className={classes.optionIcon} />}>

      <Typography className={classes.optionTitle}>{keys && optionKey} {optionValue}</Typography>

      <input hidden value={optionKey} ref={ref} onChange={doNothing} />
    </Button>)

}

const CheckboxList: React.FC<GInputBaseAnyProps & GInputMultilistProps> = (props) => {
  const { datasource } = props;
  const classes = useUtilityClasses(props.id, props.variant);
  return (
    <GInput className={classes.input}>
      
      <div className={classes.list}>
      {datasource.entries.map(({ key, value }) => (
        <Checkbox key={key} optionValue={value} optionKey={key} ownerState={props} />
      ))}
      </div>
    </GInput>
  );
}

