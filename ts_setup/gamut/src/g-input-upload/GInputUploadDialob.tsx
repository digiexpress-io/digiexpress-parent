import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputUpload } from './GInputUpload';



export const GInputUploadDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store }) => {
  const errors = store.form.toErrors(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const newValue = event.target.value;
    store.setAnswer(element.id, newValue);
  }

  return (
    <GInputUpload
      id={element.id}
      label={element.label}
      description={desc}
      errors={errors}
      value={element.value}
      variant={'upload'}
      labelPosition={labelPosition}
      onChange={onChange}
    />);
}