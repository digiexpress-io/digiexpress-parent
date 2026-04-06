import React from 'react';
import { FsDirentServiceProps } from './FsDirentServiceProps';
import { FsDirentServiceCreate } from './FsDirentServiceCreate';
import { FsDirentServiceUpdate } from './FsDirentServiceUpdate';

export const FsDirentService: React.FC<FsDirentServiceProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentServiceUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentServiceCreate parentFolder={tab.parentFolder} pathToTopParent={tab.pathToTopParent} />;
};
