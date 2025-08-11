import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputAddress } from './GInputAddress';



export const GInputAddressDialob: React.FC<GFormBaseElementProps> = ({ disabled, actionItem: element, formStore: store }) => {
  const errors = store.form.toErrors(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const newValue = event.target.value;
    store.setAnswer(element.id, newValue);
  }

  return (
    <GInputAddress
      id={element.id}
      disabled={disabled}
      label={element.label}
      description={desc}
      errors={errors}
      value={element.value}
      variant='address'
      onChange={onChange}
      labelPosition={labelPosition}
    />);
}