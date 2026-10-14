import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputUnit } from './GInputUnit';


export const GInputUnitDialob: React.FC<GFormBaseElementProps> = ({ disabled, actionItem: element, formStore: store, navRef, navRefId }) => {
  const errors = store.form.toErrors(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    store.setAnswer(element.id, event.target.value);
  }

  return (
    <>
      <div ref={navRef} id={navRefId} />
      <GInputUnit
        disabled={disabled}
        id={element.id}
        label={store.form.toLabel(element.id)}
        description={desc}
        errors={errors}
        required={!!element.required}
        value={element.value}
        variant='unit'
        unitLabel={element.props?.unitLabel}
        labelPosition={labelPosition}
        onChange={onChange}
        readOnly={element.readOnly}
      />
    </>
  );
}
