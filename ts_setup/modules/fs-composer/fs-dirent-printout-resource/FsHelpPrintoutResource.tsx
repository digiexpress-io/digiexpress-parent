import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpPrintoutResource } from './help-printout-resource';

export const FsHelpPrintoutResource: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpPrintoutResource} />;
};
