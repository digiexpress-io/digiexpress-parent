import React from 'react';

import { DialobApi } from '../api-dialob'
import { UNDEFINED_SELECTION_VALUE, useSlot } from './useSlot'
import { useDefaultProps } from '@mui/material/DefaultPropsProvider';



export interface GFormBaseElementClasses {
  root: string;
  variant: string;
}
export type GFormBaseElementClassKey = keyof GFormBaseElementClasses;

export interface GFormBaseElementProps {
  actionItem: DialobApi.ActionItem;
  form: DialobApi.Form;
  formStore: DialobApi.FormStore;
  children?: React.ReactNode | undefined; 
  onAfterComplete: () => void;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;

  // custom override, applied first if undefined fallback to system default
  component?: ((props: GFormBaseElementProps) => [React.ElementType<any>, any] | undefined ) | undefined;

}

const MUI_NAME = 'GFormBaseElement';

export const GFormBaseElement: React.FC<GFormBaseElementProps> = (initProps) => {
  const props = useDefaultProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { onChange } = props;

  // resolve override
  const [Slot, slotProps] = (props.component ? props.component(props) : undefined) ?? useSlot(props);

  return (<Slot {...slotProps} onChange={onChange}>{props.children}</Slot>);
}