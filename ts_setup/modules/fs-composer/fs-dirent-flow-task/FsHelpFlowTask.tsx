import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpFlowTask } from './help-flow-task';

export const FsHelpFlowTask: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpFlowTask} />;
};
