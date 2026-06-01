import React from 'react';
import { FsDirentPrintoutPageProps } from './FsDirentPrintoutPageProps';
import { FsDirentPrintoutPageCreate } from './FsDirentPrintoutPageCreate';

export const FsDirentPrintoutPage: React.FC<FsDirentPrintoutPageProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <></>;
  }
  return <FsDirentPrintoutPageCreate />;
};
