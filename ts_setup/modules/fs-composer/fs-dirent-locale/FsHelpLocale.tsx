import React from 'react';
import { FsHelpMarkdown } from '../fs-panel-help';
import { helpLocale } from './help-locale';

export const FsHelpLocale: React.FC<{ direntId: string }> = () => {
  return <FsHelpMarkdown content={helpLocale} />;
};
