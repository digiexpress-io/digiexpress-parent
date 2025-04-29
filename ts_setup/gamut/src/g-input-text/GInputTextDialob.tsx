import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputText } from './GInputText';



export const GInputTextDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store }) => {
  const errors = store.form.toErrors(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const newValue = event.target.value;
    store.setAnswer(element.id, newValue);
  }

  return (
    <GInputText 
      id={element.id}
      label={element.label}
      description={desc}
      errors={errors}
      value={element.value}
      variant='text'
      labelPosition={labelPosition}
      onChange={onChange}
    />);
}
