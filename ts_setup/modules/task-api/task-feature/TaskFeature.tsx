import React from 'react';
import { TaskApi } from '../task-types';
import { useTaskFeatures } from './TaskFeatureProvider';


function oneOf(type: TaskApi.TaskFeatureType[]): (input: TaskApi.TaskFeatureType[]) => boolean {
  return (input) => {
    return !!input.find((permission) => type.includes(permission))
  };
}
function notOneOf(type: TaskApi.TaskFeatureType[]): (input: TaskApi.TaskFeatureType[]) => boolean {
  return (input) => {
    return !input.find((permission) => type.includes(permission))
  };
}


const TaskFeatureMapping = {
  'TASK_FEEDBACK': oneOf(['feedback']),
  'TASK_TRANSFER': oneOf(['transfer']),
  'CRM_MESSAGES': notOneOf(['anon']),
  'ASSIGNABLE': notOneOf(['assignable']),

}

export type TaskFeatureType = keyof typeof TaskFeatureMapping;
export const TaskFeature: React.FC<{ children: React.ReactNode, id: TaskFeatureType }> = ({ children, id }) => {
  const { features } = useTaskFeatures();
  const required = TaskFeatureMapping[id];
  const isEnabled = required(features);

  if (id === 'CRM_MESSAGES') {
    //console.log(id, { isEnabled, required }, features.map(f => f))
  }
  if (isEnabled) {
    return <>{children}</>
  }
  return (<></>)
}