import React from 'react';
import { FsDirentPrintoutPageProps } from './FsDirentPrintoutPageProps';
import { FsDirentPrintoutPageCreate } from './FsDirentPrintoutPageCreate';
import { FsDirentPrintoutPageUpdate } from './FsDirentPrintoutPageUpdate';

export const FsDirentPrintoutPage: React.FC<FsDirentPrintoutPageProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentPrintoutPageUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentPrintoutPageCreate />;
};
