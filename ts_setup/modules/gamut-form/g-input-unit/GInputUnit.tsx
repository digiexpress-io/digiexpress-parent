import React from 'react'

import { OverridableStringUnion } from '@mui/types'
import { InputAdornment, TextField, useThemeProps } from '@mui/material'

import { DialobApi } from '@dxs-ts/gamut-api'
import { GInputError } from '../g-input-error'
import { GInputLabel } from '../g-input-label'
import { GInputAdornment } from '../g-input-adornment'
import { GInputBase, GInputBaseAnyProps, GInputBaseProps } from '../g-input-base'

import { MUI_NAME, GInputUnitRoot, useUtilityClasses } from './useUtilityClasses'


export interface GInputUnitPropsVariantOverrides { }

export interface GInputUnitProps {
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

  unitLabel: string | undefined;

  variant: OverridableStringUnion<
    'unit',
    GInputUnitPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'unit',
    GInputUnitPropsVariantOverrides>,
    React.ElementType>;

  component?: React.ElementType<GInputUnitProps>;
}


export const GInputUnit: React.FC<GInputUnitProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { variant = 'unit', labelPosition, errors } = props;
  const classes = useUtilityClasses(props.id, variant);
  const ownerState = { ...props, variant };

  const { id, label, description } = props;
  const slots: GInputBaseProps<GInputUnitProps> = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      adornment: GInputAdornment,
      input: props.readOnly ? ReadOnlyUnitInput : UnitInput,
    },
    slotProps: {
      error: { id, errors },
      input: { name: id, ...props },
      label: { id, children: label ?? '', labelPosition, required: props.required, errors: props.errors },
      adornment: { id, children: description, title: label ?? '', disabled: props.disabled }
    }
  }

  return (
    <GInputUnitRoot className={classes.root} ownerState={ownerState} as={props.component}>
      <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
    </GInputUnitRoot>
  );
}


const ReadOnlyUnitInput: React.FC<GInputBaseAnyProps & GInputUnitProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  return (
    <TextField value={props.value ? toDisplayValue(props.value) : ''} className={classes.input}
      slotProps={{
        input: {
          readOnly: true,
          endAdornment: props.unitLabel ? <InputAdornment position="end">{props.unitLabel}</InputAdornment> : undefined,
        }
      }}
    />
  );
}

function toRawDecimal(raw: string): string {
  return raw.replace(',', '.').replace(/[^0-9.]/g, '');
}

function toDisplayValue(raw: string): string {
  return raw.replace('.', ',');
}

const UnitInput: React.FC<GInputBaseAnyProps & GInputUnitProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  const [displayValue, setDisplayValue] = React.useState(() => toDisplayValue(props.value ?? ''));

  function handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const decimal = toRawDecimal(event.target.value);
    setDisplayValue(toDisplayValue(decimal));
    const synthetic = { ...event, target: { ...event.target, value: decimal } };
    props.onChange(synthetic as React.ChangeEvent<HTMLInputElement>);
  }

  return (
    <TextField
      disabled={props.disabled}
      value={displayValue}
      name={props.name}
      onChange={handleChange}
      className={classes.input}
      error={(props.errors?.length ?? 0) > 0}
      inputMode="decimal"
      slotProps={{
        input: {
          endAdornment: props.unitLabel ? <InputAdornment position="end">{props.unitLabel}</InputAdornment> : undefined,
        }
      }}
    />
  );
}
