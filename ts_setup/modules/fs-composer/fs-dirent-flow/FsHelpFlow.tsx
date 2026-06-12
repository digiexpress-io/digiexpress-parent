import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpFlow } from './help-flow';

export const FsHelpFlow: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpFlow} />;
};
