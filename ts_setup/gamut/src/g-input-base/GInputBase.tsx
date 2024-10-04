import React from 'react'
import { Grid } from '@mui/material'
import { GInputLabelProps } from '../g-input-label'
import { GInputErrorProps } from '../g-input-error'
import { GInputAdornmentProps } from '../g-input-adornment'
import { useThemeInfra, GInputBaseRoot } from './useThemeInfra'




export interface GInputBaseClasses {
  root: string;
  label: string;
  error: string;
  input: string;
}
export type GInputBaseClassKey = keyof GInputBaseClasses;

export interface GInputBaseAnyProps {
  name: string;
  onChange?: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}

export type LabelPosition = 'label-left' | 'label-top';

export interface GInputBaseProps<InputProps = {}> {
  id: string;
  slots: {
    label: React.ElementType<GInputLabelProps>,
    error: React.ElementType<GInputErrorProps>;
    input: React.ElementType<GInputBaseAnyProps & InputProps>;
    adornment?: React.ElementType<GInputAdornmentProps>;
    secondary?: React.ElementType<GInputBaseAnyProps & InputProps>;
  };
  slotProps: {
    label: GInputLabelProps,
    error: GInputErrorProps;
    input: GInputBaseAnyProps & InputProps;
    adornment?: GInputAdornmentProps;
    secondary?: GInputBaseAnyProps & InputProps;
  };
  component?: React.ElementType<GInputBaseProps & InputProps>;
}


export function GInputBase<InputProps = {}>(initProps: GInputBaseProps<InputProps>) {
  const { props, classes, ownerState } = useThemeInfra<InputProps>(initProps);
  const Input: React.ElementType = props.slots.input;
  const Error: React.ElementType = props.slots.error;
  const Label: React.ElementType = props.slots.label;
  const Sec: React.ElementType | undefined = props.slots.secondary;
  const Adornment: React.ElementType = props.slots.adornment ?? (() => <></>);
  const small = props.slotProps.label.labelPosition === 'label-left' ? 6 : 12;

  return (
    <GInputBaseRoot as={ownerState.component} className={classes.root} spacing={1} container ownerState={ownerState}>
      
      <Grid item xl={small} lg={small} md={small} sm={12} xs={12} className={classes.label}>
        <Label {...props.slotProps.label} />
        <Adornment {...props.slotProps.adornment} />
      </Grid>
      <Grid item xl={small} lg={small} md={small} sm={12} xs={12} className={classes.input}>
        <Input {...props.slotProps.input} />
      </Grid>

      {(props.slotProps.error.errors?.length ?? 0) > 0 && <Grid item xl={12} lg={12} md={12} sm={12} xs={12} className={classes.error}>
        <Error {...props.slotProps.error} />
      </Grid>}

      {Sec &&
        (<Grid item xl={12} lg={12} md={12} sm={12} xs={12}>
          <Sec {...props.slotProps.secondary}/>
        </Grid>)}

    </GInputBaseRoot>
  )
}