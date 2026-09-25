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


const FI_FORMAT = new Intl.NumberFormat('fi-FI', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

function formatFinnish(raw: string): string {
  const numeric = parseFloat(raw);
  if (isNaN(numeric)) {
    return '0,00';
  }
  return FI_FORMAT.format(numeric);
}

function formatWhileTyping(raw: string): string {
  const parts = raw.split('.');
  const intPart = parts[0] ? new Intl.NumberFormat('fi-FI').format(parseInt(parts[0], 10)) : '';
  return parts.length === 2 ? intPart + ',' + parts[1] : intPart;
}

function toRawDecimal(raw: string): string {
  const cleaned = raw.replace(/\s/g, '').replace(',', '.');
  const sanitized = cleaned.replace(/[^0-9.]/g, '');
  const parts = sanitized.split('.');
  if (parts.length > 2) {
    return parts[0] + '.' + parts.slice(1).join('');
  }
  if (parts.length === 2 && parts[1].length > 2) {
    return parts[0] + '.' + parts[1].slice(0, 2);
  }
  return sanitized;
}


const ReadOnlyCurrencyInput: React.FC<GInputBaseAnyProps & GInputCurrencyProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  const display = props.value ? formatFinnish(props.value) : '--';
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
  const inputRef = React.useRef<HTMLInputElement>(null);
  const [displayValue, setDisplayValue] = React.useState(() => formatFinnish(props.value ?? '0.00'));
  const cursorOffset = React.useRef<number | null>(null);

  React.useLayoutEffect(() => {
    if (cursorOffset.current !== null && inputRef.current) {
      inputRef.current.setSelectionRange(cursorOffset.current, cursorOffset.current);
      cursorOffset.current = null;
    }
  });

  function handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const raw = event.target.value;
    const cursor = (event.target as HTMLInputElement).selectionStart ?? raw.length;

    const decimal = toRawDecimal(raw);
    const formatted = formatWhileTyping(decimal);

    const spacesBeforeCursorOld = (raw.slice(0, cursor).match(/\s/g) ?? []).length;
    const spacesBeforeCursorNew = (formatted.slice(0, cursor).match(/\s/g) ?? []).length;
    cursorOffset.current = cursor + (spacesBeforeCursorNew - spacesBeforeCursorOld);

    setDisplayValue(formatted);
    const synthetic = { ...event, target: { ...event.target, value: decimal } };
    props.onChange(synthetic as React.ChangeEvent<HTMLInputElement>);
  }

  function handleBlur() {
    setDisplayValue(formatFinnish(props.value ?? '0.00'));
  }

  return (
    <TextField
      disabled={props.disabled}
      value={displayValue}
      name={props.name}
      inputRef={inputRef}
      onChange={handleChange}
      onBlur={handleBlur}
      className={classes.input}
      error={(props.errors?.length ?? 0) > 0}
      inputMode="decimal"
      slotProps={{
        input: {
          startAdornment: props.currency ? <InputAdornment position="start">{props.currency}</InputAdornment> : undefined,
        }
      }}
    />
  );
}
