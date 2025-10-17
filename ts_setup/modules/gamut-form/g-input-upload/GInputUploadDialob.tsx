import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputUpload } from './GInputUpload';



export const GInputUploadDialob: React.FC<GFormBaseElementProps> = ({ disabled, actionItem: element, formStore: store, navRef, navRefId }) => {
  const errors = store.form.toErrors(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const newValue = event.target.value;
    store.setAnswer(element.id, newValue);
  }

  return (
    <>
      <div ref={navRef} id={navRefId} />
      <GInputUpload
        id={element.id}
        disabled={disabled}
        label={store.form.toLabel(element.id)}
        description={desc}
        errors={errors}
        value={element.value}
        variant={'upload'}
        required={!!element.required}
        labelPosition={labelPosition}
        onChange={onChange}
      />
    </>
  );
}