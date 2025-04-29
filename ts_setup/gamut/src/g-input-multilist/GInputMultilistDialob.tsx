import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputMultilist } from './GInputMultilist';



export const GInputMultilistDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store }) => {
  const valueset = store.form.toValueSet(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);
  const errors = store.form.toErrors(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {      
    const multichoiceEvent = event as React.ChangeEvent<HTMLInputElement>;
    const targetValue = multichoiceEvent.target.value as string;

    const oldValue: string[] = element.value ?? [];
    const newValue: string[] = oldValue.includes(targetValue) ? 
      oldValue.filter(v => v !== targetValue) : 
      [...oldValue, targetValue];

    store.setAnswer(store.id, newValue);
  }

  return (
    <GInputMultilist
      id={element.id}
      label={element.label}
      description={desc}
      variant='multilist'
      errors={errors}
      value={element.value}
      datasource={valueset!}
      onChange={onChange}
      labelPosition={labelPosition}
    />);
}
