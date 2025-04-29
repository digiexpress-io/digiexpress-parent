import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputSurveyQuestion } from './GInputSurveyQuestion';


export const GInputSurveyQuestionDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store }) => {

  const description = store.form.toDescription(element.id);
  const parent = store.form.toParent(element.id)
  const options = parent ? store.form.toValueSet(parent.id) : undefined;
  const questions = parent ? store.form.toChildren(parent?.id) : [];
  const index = questions.map(item => item.id).indexOf(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const newValue = event.target.value;
    store.setAnswer(element.id, newValue);
  }

  return (<GInputSurveyQuestion 
    id={element.id}
    label={element.label}
    description={description}
    index={index}
    value={element.value}
    options={options?.entries.map(e => ({ id: e.key, label: e.value, description: undefined })) ?? []}
    onChange={onChange}
  />)
}