import React from 'react';
import { WorkflowTable } from './WorkflowTable';
import { useFetch } from '../../hooks/useFetch';
import { Workflow } from '../../types/Workflow';
import { useConfig } from '../../context/ConfigContext';

export const WorkflowView: React.FC = () => {
  const config = useConfig();
  const apiUrl = config.wrenchApiUrl;
  const { response: workflows, refresh: refreshWorkflows } = useFetch<Workflow[]>(`${apiUrl}/workflows/`);

  return (
    <WorkflowTable workflows={workflows} refreshWorkflows={refreshWorkflows} />
  );
}