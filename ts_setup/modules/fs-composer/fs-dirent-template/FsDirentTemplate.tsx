import React from 'react';
import { FsDirentTemplateProps } from './FsDirentTemplateProps';
import { FsDirentTemplateCreate } from './FsDirentTemplateCreate';
import { FsDirentTemplateUpdate } from './FsDirentTemplateUpdate';


export const FsDirentTemplate: React.FC<FsDirentTemplateProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentTemplateUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentTemplateCreate parentFolder={tab.parentFolder} pathToTopParent={tab.pathToTopParent} />;
};
