import React from 'react';
import { FsDirentArticlePageProps } from './FsDirentArticlePageProps';
import { FsDirentArticlePageCreate } from './FsDirentArticlePageCreate';
import { FsDirentArticlePageUpdate } from './FsDirentArticlePageUpdate';


export const FsDirentArticlePage: React.FC<FsDirentArticlePageProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentArticlePageUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentArticlePageCreate />;
};
