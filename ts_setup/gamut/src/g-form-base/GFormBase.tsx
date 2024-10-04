import React from 'react';
import { useThemeInfra, GFormBaseRoot } from './useThemeInfra'
import { GOverridableComponent } from '../g-override';
import { GFormBaseElement, UNDEFINED_SELECTION_VALUE } from '../g-form-base-element';

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

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    if(actionItem.type === 'boolean') {    

      const newValue: 'true' | 'false' | '' = event.target.value + '' as any;
      if(newValue === 'true') {
        formStore.setAnswer(actionItem.id, true);
      } else if(newValue === 'false') {
        formStore.setAnswer(actionItem.id, false);
      } else {
        formStore.setAnswer(actionItem.id, undefined);
      }
      
    } else if(actionItem.type === 'list') {

      const newValue = event.target.value === UNDEFINED_SELECTION_VALUE ? undefined : event.target.value;
      formStore.setAnswer(actionItem.id, newValue);

    } else if(actionItem.type === 'multichoice') {
    
      const multichoiceEvent = event as React.ChangeEvent<HTMLInputElement>;
      const targetValue = multichoiceEvent.target.value as string;

      const oldValue: string[] = actionItem.value ?? [];
      const newValue: string[] = oldValue.includes(targetValue) ? 
        oldValue.filter(v => v !== targetValue) : 
        [...oldValue, targetValue];

        formStore.setAnswer(actionItem.id, newValue);

    } else if(actionItem.type === 'text') {

      const newValue = event.target.value;
      formStore.setAnswer(actionItem.id, newValue);

    } else if(actionItem.type === 'survey') {
      
      const newValue = event.target.value;
      formStore.setAnswer(actionItem.id, newValue);

    } else if(actionItem.type === 'decimal') {

      const newValue = event.target.value;
      formStore.setAnswer(actionItem.id, newValue);

    } else if(actionItem.type === 'number') {

      const newValue = event.target.value;
      formStore.setAnswer(actionItem.id, newValue);

    } else if(actionItem.type === 'date') {

      const newValue = event.target.value;
      formStore.setAnswer(actionItem.id, newValue ? newValue : undefined);

    } else if(actionItem.type === 'time') {

      const newValue = event.target.value;
      formStore.setAnswer(actionItem.id, newValue ? newValue : undefined);
    } else {
      //console.warn('skipping onChange for', element);
    }
  }


  if(ownerState.unwrap) {
    return (<GFormBaseElement onChange={onChange} onAfterComplete={onAfterComplete} formStore={formStore} form={form} actionItem={actionItem}>{props.children}</GFormBaseElement>);
  }

  const Root = ownerState.component ?? GFormBaseRoot;
  return (<Root className={classes.root} ownerState={ownerState}>
    <GFormBaseElement onChange={onChange} onAfterComplete={onAfterComplete} formStore={formStore} actionItem={actionItem} form={form}>{props.children}</GFormBaseElement>
  </Root>);
}