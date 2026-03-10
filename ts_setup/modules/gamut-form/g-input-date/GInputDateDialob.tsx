import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputDate } from './GInputDate';



export const GInputDateDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store, disabled, navRef, navRefId }) => {

  const errors = store.form.toErrors(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const newValue = event.target.value;
    store.setAnswer(element.id, newValue ? newValue : undefined);
  }

  console.log(element.id, element.readOnly + "")

  return (
    <>
      <div ref={navRef} id={navRefId} />
      <GInputDate
        disabled={disabled}
        id={element.id}
        variant='date'
        label={store.form.toLabel(element.id)}
        description={desc}
        errors={errors}
        value={element.value}
        labelPosition={labelPosition}
        format={undefined}
        required={!!element.required}
        onChange={onChange}
        readOnly={element.readOnly}
      />
    </>);
}