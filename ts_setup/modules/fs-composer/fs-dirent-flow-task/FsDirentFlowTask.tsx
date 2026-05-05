import React from 'react';
import { FsDirentFlowTaskProps } from './FsDirentFlowTaskProps';
import { FsDirentFlowTaskCreate } from './FsDirentFlowTaskCreate';
import { FsDirentFlowTaskUpdate } from './FsDirentFlowTaskUpdate';


export const FsDirentFlowTask: React.FC<FsDirentFlowTaskProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentFlowTaskUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentFlowTaskCreate parentFolder={tab.parentFolder} />;
};
