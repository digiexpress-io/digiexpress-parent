import React from 'react';
import { FsDirentPrintoutResourceProps } from './FsDirentPrintoutResourceProps';
import { FsDirentPrintoutResourceCreate } from './FsDirentPrintoutResourceCreate';
import { FsDirentPrintoutResourceUpdate } from './FsDirentPrintoutResourceUpdate';

export const FsDirentPrintoutResource: React.FC<FsDirentPrintoutResourceProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentPrintoutResourceUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentPrintoutResourceCreate />;
};
