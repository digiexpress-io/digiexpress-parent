import React from 'react';
import { GFormBaseElementProps, UNDEFINED_SELECTION_VALUE } from '../g-form-base-element';
import { GInputList } from './GInputList';



export const GInputListDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store }) => {

  const valueset = store.form.toValueSet(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);
  const errors = store.form.toErrors(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const newValue = event.target.value === UNDEFINED_SELECTION_VALUE ? undefined : event.target.value;
    store.setAnswer(element.id, newValue);
  }
  const variant = element.props?.variant === 'radio' ? 'list-radio' : 'list';
  return (
    <GInputList
      id={element.id}
      label={element.label}
      description={desc}
      errors={errors}
      variant={variant}
      undefinedValue={UNDEFINED_SELECTION_VALUE}
      value={element.value ?? UNDEFINED_SELECTION_VALUE}
      datasource={valueset}
      onChange={onChange}
      labelPosition={labelPosition}
    />);
}
