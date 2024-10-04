import React from 'react';
import { OverridableStringUnion } from '@mui/types';
import { TextField, useThemeProps } from '@mui/material';
import numbro from 'numbro';

import { DialobApi } from '../api-dialob';
import { GInputBase, GInputBaseAnyProps, GInputBaseProps, LabelPosition } from '../g-input-base';
import { GInputError } from '../g-input-error';
import { GInputLabel } from '../g-input-label';
import { GInputAdornment } from '../g-input-adornment';

import { MUI_NAME, useUtilityClasses, GInputIntRoot } from './useUtilityClasses';
import { InputHidden } from './InputHidden';


// extension hook for adding custom input types
export interface GInputIntPropsVariantOverrides { };

export interface GInputIntProps {
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
    'int',
    GInputIntPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'int',
    GInputIntPropsVariantOverrides>,
    React.ElementType>; 

  component?: React.ElementType<GInputIntProps>;
}


export const GInputInt: React.FC<GInputIntProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { id, label, variant = 'int', labelPosition, errors } = props;
  const ownerState = { ...props, variant };
  const classes = useUtilityClasses(id, variant);

  const slots: GInputBaseProps<GInputIntProps> =  {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      input: IntInput,
      adornment: GInputAdornment
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState, name: id },
      label: { id, children: label ?? '', labelPosition },
      adornment: { id, children: props.description, title: label }
    }
  }

  return (<GInputIntRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputIntRoot>);
}


const DEFAULT_FORMAT: numbro.Format = {
  thousandSeparated: true,
  mantissa: 0,
  //negative: 'parenthesis'
}



const IntInput: React.FC<GInputBaseAnyProps & GInputIntProps> = (props) => {

  const [value, setValue] = React.useState(props.value);
  const themeFormat = props.format ? props.format(props.id) : undefined;
  const finalFormat = themeFormat ?? DEFAULT_FORMAT;

  function format(value: string | undefined): string {
    if(value === '' || value === undefined || value === null) {
      return '';
    }
    if(props.formatter) {
      return props.formatter(props.id, value);
    }

    const result = numbro(value).format(finalFormat);
    return result;
  }


  function handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const caret = event.target.selectionStart
    const element = event.target
    window.requestAnimationFrame(() => {
      element.selectionStart = caret
      element.selectionEnd = caret
    })
    const newValue = format(event.target.value);
    setValue(newValue);
  }

  return (<>
    <InputHidden id={props.id} value={value} format={finalFormat} onChange={props.onChange}/>
    <TextField value={value} onChange={handleChange} error={(props.errors?.length ?? 0) > 0} />
    </>
  )
}