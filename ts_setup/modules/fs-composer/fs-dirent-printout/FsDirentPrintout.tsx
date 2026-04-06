import React from 'react';
import { FsDirentPrintoutProps } from './FsDirentPrintoutProps';
import { FsDirentPrintoutCreate } from './FsDirentPrintoutCreate';
import { FsDirentPrintoutUpdate } from './FsDirentPrintoutUpdate';


export const FsDirentPrintout: React.FC<FsDirentPrintoutProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentPrintoutUpdate direntId={tab.dirent.id} pathToTopParent={tab.pathToTopParent} />;
  }
  return <FsDirentPrintoutCreate parentFolder={tab.parentFolder} pathToTopParent={tab.pathToTopParent} />;
};
