import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputBoolean } from './GInputBoolean';



export const GInputBooleanDialob: React.FC<GFormBaseElementProps> = ({ disabled, actionItem: element, formStore: store }) => {
  const errors = store.form.toErrors(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const newValue: 'true' | 'false' | '' = event.target.value + '' as any;

    if(newValue === 'true') {
      store.setAnswer(element.id, true);
    } else if(newValue === 'false') {
      store.setAnswer(element.id, false);
    } else {
      store.setAnswer(element.id, undefined);
    }
  }
  return (
    <GInputBoolean
      id={element.id}
      disabled={disabled}
      label={element.label}
      description={desc}
      variant='checkbox'
      errors={errors}
      value={element.value}
      onChange={onChange}
      labelPosition={labelPosition}
    />);
}
