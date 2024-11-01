import React from 'react';
import { WorkflowTable } from './WorkflowTable';
import { useFetch } from '../../hooks/useFetch';
import { Workflow } from '../../types/Workflow';
import { useConfig } from '../../context/ConfigContext';

export const WorkflowView: React.FC = () => {
  const { serviceUrl } = useConfig();
  const { response: workflows, refresh: refreshWorkflows } = useFetch<Workflow[]>(`${serviceUrl}rest/api/assets/workflows`);

  return (
    <WorkflowTable workflows={workflows} refreshWorkflows={refreshWorkflows} />
  );
}