

import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputSurvey } from './GInputSurvey';



export const GInputSurveyDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store, children }) => {

  const errors = store.form.toErrors(element.id);
  const description = store.form.toDescription(element.id);
  const options = store.form.toValueSet(element.id);
  const questions = store.form.toChildren(element.id);
  const vertical = element.view === 'verticalSurveygroup';
  const labelPosition = store.form.toLabelPosition(element.id);
  const border: boolean | undefined = element.props?.border ? (element.props?.border === 'true') : undefined;

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {

  }

  return (<GInputSurvey
    id={element.id}
    label={element.label}
    options={options?.entries.map(e => ({ id: e.key, label: e.value, description: undefined })) ?? []}
    questions={questions.map(e => ({
      label: e.label ?? '',
      description: store.form.toDescription(e.id),
      id: e.id,
      value: e.value as any
    }))}
    border={border}
    description={description}
    children={children}
    vertical={vertical}
    errors={errors}
    onChange={onChange}
    labelPosition={labelPosition} />);
}