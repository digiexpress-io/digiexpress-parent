import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpDecisionTable } from './help-decision-table';

export const FsHelpDecisionTable: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpDecisionTable} />;
};
