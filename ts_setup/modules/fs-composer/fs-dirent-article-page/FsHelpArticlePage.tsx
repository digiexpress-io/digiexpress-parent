import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpArticlePage } from './help-article-page';

export const FsHelpArticlePage: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpArticlePage} />;
};
