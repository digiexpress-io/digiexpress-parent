import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpFolder } from './help-folder';

export const FsHelpFolder: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpFolder} />;
};
