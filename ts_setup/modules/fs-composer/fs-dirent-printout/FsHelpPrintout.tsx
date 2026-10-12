import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpPrintout } from './help-printout';

export const FsHelpPrintout: React.FC<{ direntId: string }> = (_props) => {
  return <FsHelpMarkdown content={helpPrintout} />;
};
