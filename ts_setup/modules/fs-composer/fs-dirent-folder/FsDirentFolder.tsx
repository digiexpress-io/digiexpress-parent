import React from 'react';
import { FsDirentFolderProps } from './FsDirentFolderProps';
import { FsDirentFolderCreate } from './FsDirentFolderCreate';
import { FsDirentFolderUpdate } from './FsDirentFolderUpdate';


export const FsDirentFolder: React.FC<FsDirentFolderProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentFolderUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentFolderCreate parentFolder={tab.parentFolder} />;
};
