import React from 'react';
import { useThemeInfra, GFormBaseRoot } from './useThemeInfra'
import { GOverridableComponent } from '@dxs-ts/gamut-api';
import { GFormBaseElement } from '../g-form-base-element';
import { useForm } from '@dxs-ts/gamut-api';

export interface GFormBaseClasses {
  root: string;
  variant: string;
}
export type GFormBaseClassKey = keyof GFormBaseClasses;

export interface GFormBaseProps {
  id: string;
  children?: React.ReactNode | undefined;
  component?: GOverridableComponent<GFormBaseProps>;
}

export const GFormBase: React.FC<GFormBaseProps> = (initProps) => {
  const {
    classes, props, ownerState,
    onAfterComplete,
    actionItem, formStore, form
  } = useThemeInfra(initProps);

  const { disabled } = useForm();

  if (ownerState.unwrap) {
    return (
      <GFormBaseElement
        disabled={disabled}
        onAfterComplete={onAfterComplete}
        formStore={formStore}
        form={form}
        actionItem={actionItem}>
        {props.children}
      </GFormBaseElement>);
  }

  const Root = ownerState.component ?? GFormBaseRoot;
  return (
    <Root className={classes.root} ownerState={ownerState}>
      <GFormBaseElement disabled={disabled} onAfterComplete={onAfterComplete} formStore={formStore} actionItem={actionItem} form={form}>{props.children}</GFormBaseElement>
    </Root>
  );
}