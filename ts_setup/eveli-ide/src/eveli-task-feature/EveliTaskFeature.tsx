import { TaskApi } from '@/api-task';
import React from 'react';
import { useTaskFeatures } from './EveliTaskFeatureProvider';

function oneOf(type: TaskApi.TaskFeatureType[]): (input: TaskApi.TaskFeatureType) => boolean {
  return (input) => type.includes(input);
}

const EveliTaskFeatureMapping = {
  'TASK_FEEDBACK': oneOf(['feedback']),
  'TASK_TRANSFER': oneOf(['transfer']),
}


export type EveliTaskFeatureType = keyof typeof EveliTaskFeatureMapping;


export const EveliTaskFeature: React.FC<{ children: React.ReactNode, id: EveliTaskFeatureType }> = ({ children, id }) => {
  const { features } = useTaskFeatures();
  const required = EveliTaskFeatureMapping[id];
  const isAccessGranted = features.find((permission) => required(permission));

  if (isAccessGranted) {
    return <>{children}</>
  }
  return (<></>)
}