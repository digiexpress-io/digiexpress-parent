import React from 'react';
import { FsDirentArticleWorkflowProps } from './FsDirentArticleWorkflowProps';
import { FsDirentArticleWorkflowCreate } from './FsDirentArticleWorkflowCreate';
import { FsDirentArticleWorkflowUpdate } from './FsDirentArticleWorkflowUpdate';

export const FsDirentArticleWorkflow: React.FC<FsDirentArticleWorkflowProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentArticleWorkflowUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentArticleWorkflowCreate />;
};
