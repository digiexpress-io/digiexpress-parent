import React from 'react';
import { FsDirentLinkProps } from './FsDirentLinkProps';
import { FsDirentLinkCreate } from './FsDirentLinkCreate';
import { FsDirentLinkUpdate } from './FsDirentLinkUpdate';


export const FsDirentLink: React.FC<FsDirentLinkProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentLinkUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentLinkCreate parentFolder={tab.parentFolder} />;
};
