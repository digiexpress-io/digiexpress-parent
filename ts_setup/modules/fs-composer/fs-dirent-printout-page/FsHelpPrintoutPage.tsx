import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpPrintoutPage } from './help-printout-page';

export const FsHelpPrintoutPage: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpPrintoutPage} />;
};
