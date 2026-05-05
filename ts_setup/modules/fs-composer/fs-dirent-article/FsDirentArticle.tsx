import React from 'react';
import { FsDirentArticleProps } from './FsDirentArticleProps';
import { FsDirentArticleCreate } from './FsDirentArticleCreate';
import { FsDirentArticleUpdate } from './FsDirentArticleUpdate';


export const FsDirentArticle: React.FC<FsDirentArticleProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentArticleUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentArticleCreate />;
};
