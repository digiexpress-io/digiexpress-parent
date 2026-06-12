import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpArticleTemplate } from './help-article-template';

export const FsHelpArticleTemplate: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpArticleTemplate} />;
};
