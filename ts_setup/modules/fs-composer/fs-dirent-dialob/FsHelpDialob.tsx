import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpDialob } from './help-dialob';

export const FsHelpDialob: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpDialob} />;
};
