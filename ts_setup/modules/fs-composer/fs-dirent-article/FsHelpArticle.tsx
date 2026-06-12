import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpArticle } from './help-article';

export const FsHelpArticle: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpArticle} />;
};
