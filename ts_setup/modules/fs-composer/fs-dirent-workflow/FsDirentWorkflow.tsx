import React from 'react';
import { FsDirentWorkflowProps } from './FsDirentWorkflowProps';
import { FsDirentWorkflowCreate } from './FsDirentWorkflowCreate';
import { FsDirentWorkflowUpdate } from './FsDirentWorkflowUpdate';

export const FsDirentWorkflow: React.FC<FsDirentWorkflowProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentWorkflowUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentWorkflowCreate parentFolder={tab.parentFolder} pathToTopParent={tab.pathToTopParent} />;
};
