import React from 'react'

import { OverridableStringUnion } from '@mui/types'
import { InputAdornment, TextField, useThemeProps } from '@mui/material'

import { DialobApi } from '@dxs-ts/gamut-api'
import { GInputError } from '../g-input-error'
import { GInputLabel } from '../g-input-label'
import { GInputAdornment } from '../g-input-adornment'
import { GInputBase, GInputBaseAnyProps, GInputBaseProps } from '../g-input-base'

import { MUI_NAME, GInputCurrencyRoot, useUtilityClasses } from './useUtilityClasses'


export interface GInputCurrencyPropsVariantOverrides { }

export interface GInputCurrencyProps {
  id: string;
  disabled: boolean;
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: DialobApi.ControlLabelPosition;
  description: string | undefined;

  required: boolean;
  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;
  readOnly?: boolean;

  currency: string | undefined;

  variant: OverridableStringUnion<
    'currency',
    GInputCurrencyPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'currency',
    GInputCurrencyPropsVariantOverrides>,
    React.ElementType>;

  component?: React.ElementType<GInputCurrencyProps>;
}


export const GInputCurrency: React.FC<GInputCurrencyProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { variant = 'currency', labelPosition, errors } = props;
  const classes = useUtilityClasses(props.id, variant);
  const ownerState = { ...props, variant };

  const { id, label, description } = props;
  const slots: GInputBaseProps<GInputCurrencyProps> = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      adornment: GInputAdornment,
      input: props.readOnly ? ReadOnlyCurrencyInput : CurrencyInput,
    },
    slotProps: {
      error: { id, errors },
      input: { name: id, ...props },
      label: { id, children: label ?? '', labelPosition, required: props.required, errors: props.errors },
      adornment: { id, children: description, title: label ?? '', disabled: props.disabled }
    }
  }

  return (
    <GInputCurrencyRoot className={classes.root} ownerState={ownerState} as={props.component}>
      <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
    </GInputCurrencyRoot>
  );
}


function formatCurrency(raw: string): string {
  const numeric = parseFloat(raw);
  if (isNaN(numeric)) {
    return '0.00';
  }
  return numeric.toFixed(2);
}

function sanitizeInput(raw: string): string {
  // strip anything that's not a digit or decimal point
  const cleaned = raw.replace(/[^0-9.]/g, '');
  // allow only one decimal point
  const parts = cleaned.split('.');
  if (parts.length > 2) {
    return parts[0] + '.' + parts.slice(1).join('');
  }
  // cap to 2 decimal digits while typing
  if (parts.length === 2 && parts[1].length > 2) {
    return parts[0] + '.' + parts[1].slice(0, 2);
  }
  return cleaned;
}


const ReadOnlyCurrencyInput: React.FC<GInputBaseAnyProps & GInputCurrencyProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  const display = props.value ? formatCurrency(props.value) : '--';
  return (
    <TextField value={display} className={classes.input}
      slotProps={{
        input: {
          readOnly: true,
          startAdornment: props.currency ? <InputAdornment position="start">{props.currency}</InputAdornment> : undefined,
        }
      }}
    />
  );
}

const CurrencyInput: React.FC<GInputBaseAnyProps & GInputCurrencyProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);

  function handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const sanitized = sanitizeInput(event.target.value);
    const synthetic = { ...event, target: { ...event.target, value: sanitized } };
    props.onChange(synthetic as React.ChangeEvent<HTMLInputElement>);
  }

  function handleBlur() {
    const formatted = formatCurrency(props.value ?? '');
    const synthetic = {
      target: { value: formatted }
    } as React.ChangeEvent<HTMLInputElement>;
    props.onChange(synthetic);
  }

  return (
    <TextField
      disabled={props.disabled}
      value={props.value ?? '0.00'}
      name={props.name}
      onChange={handleChange}
      onBlur={handleBlur}
      className={classes.input}
      error={(props.errors?.length ?? 0) > 0}
      inputMode="decimal"
      slotProps={{
        input: {
          startAdornment: props.currency  ? <InputAdornment position="start">{props.currency}</InputAdornment> : undefined,
        }
      }}
    />
  );
}
