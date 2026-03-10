import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputText } from './GInputText';



export const GInputTextDialob: React.FC<GFormBaseElementProps> = ({ disabled, actionItem: element, formStore: store, navRef, navRefId }) => {
  const errors = store.form.toErrors(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const newValue = event.target.value;
    store.setAnswer(element.id, newValue);
  }

  return (<>
    <div ref={navRef} id={navRefId} />
    <GInputText
      disabled={disabled}
      id={element.id}
      label={store.form.toLabel(element.id)}
      description={desc}
      errors={errors}
      required={!!element.required}
      value={element.value}
      variant='text'
      labelPosition={labelPosition}
      onChange={onChange}
      readOnly={element.readOnly}
    />
  </>);
}
