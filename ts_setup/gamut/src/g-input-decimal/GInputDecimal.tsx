import React from 'react';
import { OverridableStringUnion } from '@mui/types';
import { TextField, useThemeProps } from '@mui/material';
import numbro from 'numbro';

import { DialobApi } from '../api-dialob';
import { GInputBase, GInputBaseAnyProps, GInputBaseProps, LabelPosition } from '../g-input-base';
import { GInputError } from '../g-input-error';
import { GInputLabel } from '../g-input-label';
import { GInputAdornment } from '../g-input-adornment';

import { MUI_NAME, useUtilityClasses, GInputDecimalRoot } from './useUtilityClasses';


// extension hook for adding custom input types
export interface GInputDecimalPropsVariantOverrides { };

export interface GInputDecimalProps {
  id: string;
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: LabelPosition,
  description: string | undefined;

  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;
  formatter?: ((id: string, value: string) => string);
  format?: (id: string) => numbro.Format;

  variant: OverridableStringUnion<
    'decimal',
    GInputDecimalPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'decimal',
    GInputDecimalPropsVariantOverrides>,
    React.ElementType>; 

  component?: React.ElementType<GInputDecimalProps>;
}


export const GInputDecimal: React.FC<GInputDecimalProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { id, label, variant = 'decimal', labelPosition, errors } = props;
  const ownerState = { ...props, variant };
  const classes = useUtilityClasses(id, variant);

  const slots: GInputBaseProps<GInputDecimalProps> =  {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      input: DecimalInput,
      adornment: GInputAdornment
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState, name: id },
      label: { id, children: label ?? '', labelPosition },
      adornment: { id, children: props.description, title: label }
    }
  }

  return (<GInputDecimalRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputDecimalRoot>);
}


const DEFAULT_FORMAT: numbro.Format = {
  thousandSeparated: true,
  mantissa: 2,
  //negative: 'parenthesis'
}



const DecimalInput: React.FC<GInputBaseAnyProps & GInputDecimalProps> = (props) => {
  const [value, setValue] = React.useState('');
  function format(value: string | undefined): string {
    if(value === '' || value === undefined || value === null) {
      return '';
    }


    if(props.formatter) {
      return props.formatter(props.id, value);
    }
    const themeFormat = props.format ? props.format(props.id) : undefined;
    const result = numbro(value).format(themeFormat ?? DEFAULT_FORMAT);
    return result;
  }


  function handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const caret = event.target.selectionStart
    const element = event.target
    window.requestAnimationFrame(() => {
      element.selectionStart = caret
      element.selectionEnd = caret
    })
    setValue(format(event.target.value));
  }

  return <TextField value={value} onChange={handleChange} error={(props.errors?.length ?? 0) > 0} />
}