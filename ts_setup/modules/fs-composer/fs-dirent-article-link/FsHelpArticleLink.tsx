import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpArticleLink } from './help-article-link';

export const FsHelpArticleLink: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpArticleLink} />;
};
