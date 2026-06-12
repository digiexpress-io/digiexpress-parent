import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpArticleWorkflow } from './help-article-workflow';

export const FsHelpArticleWorkflow: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpArticleWorkflow} />;
};
