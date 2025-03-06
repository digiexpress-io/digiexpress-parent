import React from 'react';
import { WorkflowTable } from './WorkflowTable';
import { useFetch } from '@dxs-ts/eveli-fetch';


export const WorkflowView: React.FC = () => {
  const { workflows, refreshWorkflows } = useFetch('worker/rest/api/assets/workflows.GET', {});
  return (
    <WorkflowTable workflows={workflows} refreshWorkflows={refreshWorkflows} />
  );
}