import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputMultilist } from './GInputMultilist';



function parseValue(value: any): string[] {
  if (Array.isArray(value)) {
    return value;
  }
  if (!value) {
    return [];
  }
  return [value];
}


export const GInputMultilistDialob: React.FC<GFormBaseElementProps> = ({ disabled, actionItem: element, formStore: store }) => {
  const valueset = store.form.toValueSet(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);
  const errors = store.form.toErrors(element.id);
  const border: boolean | undefined = element.props?.border ? (element.props?.border === 'true') : undefined;

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {      
    const multichoiceEvent = event as React.ChangeEvent<HTMLInputElement>;
    const newValue: string = multichoiceEvent.target.value;
    store.setAnswer(element.id, newValue ? newValue.split(',') : []);
  }

  return (
    <GInputMultilist
      id={element.id}
      disabled={disabled}
      label={element.label}
      description={desc}
      variant={element.props?.variant ?? 'multilist'}
      errors={errors}
      border={border}
      value={parseValue(element.value)}
      datasource={valueset!}
      onChange={onChange}
      labelPosition={labelPosition}
    />);
}
