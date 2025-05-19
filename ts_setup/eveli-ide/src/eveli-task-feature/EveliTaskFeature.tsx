import { TaskApi } from '@/api-task';
import React from 'react';
import { useTaskFeatures } from './EveliTaskFeatureProvider';

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


const EveliTaskFeatureMapping = {
  'TASK_FEEDBACK': oneOf(['feedback']),
  'TASK_TRANSFER': oneOf(['transfer']),
  'CRM_MESSAGES': notOneOf(['anon']),
}


//   const isAnonCustomer = !!props.task.clientIdentificator; // Anonymous customer in gamut (not authenticated)


export type EveliTaskFeatureType = keyof typeof EveliTaskFeatureMapping;


export const EveliTaskFeature: React.FC<{ children: React.ReactNode, id: EveliTaskFeatureType }> = ({ children, id }) => {
  const { features } = useTaskFeatures();
  const required = EveliTaskFeatureMapping[id];
  const isEnabled = required(features);

  if (id === 'CRM_MESSAGES') {
    console.log(id, { isEnabled, required }, features.map(f => f))
  }
  if (isEnabled) {
    return <>{children}</>
  }
  return (<></>)
}