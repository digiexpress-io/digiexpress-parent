import React from 'react';
import { FsDirentArticleLinkProps } from './FsDirentArticleLinkProps';
import { FsDirentArticleLinkCreate } from './FsDirentArticleLinkCreate';
import { FsDirentArticleLinkUpdate } from './FsDirentArticleLinkUpdate';


export const FsDirentArticleLink: React.FC<FsDirentArticleLinkProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentArticleLinkUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentArticleLinkCreate />;
};
