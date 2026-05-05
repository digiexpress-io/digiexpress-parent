import React from 'react';
import { FsDirentFlowProps } from './FsDirentFlowProps';
import { FsDirentFlowCreate } from './FsDirentFlowCreate';
import { FsDirentFlowUpdate } from './FsDirentFlowUpdate';


export const FsDirentFlow: React.FC<FsDirentFlowProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentFlowUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentFlowCreate parentFolder={tab.parentFolder} />;
};
