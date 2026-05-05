import React from 'react';
import { FsDirentPageProps } from './FsDirentPageProps';
import { FsDirentPageCreate } from './FsDirentPageCreate';
import { FsDirentPageUpdate } from './FsDirentPageUpdate';


export const FsDirentPage: React.FC<FsDirentPageProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentPageUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentPageCreate parentFolder={tab.parentFolder} />;
};
