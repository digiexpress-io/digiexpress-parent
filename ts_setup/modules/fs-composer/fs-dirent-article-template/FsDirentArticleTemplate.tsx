import React from 'react';
import { FsDirentArticleTemplateProps } from './FsDirentArticleTemplateProps';
import { FsDirentArticleTemplateCreate } from './FsDirentArticleTemplateCreate';
import { FsDirentArticleTemplateUpdate } from './FsDirentArticleTemplateUpdate';


export const FsDirentArticleTemplate: React.FC<FsDirentArticleTemplateProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentArticleTemplateUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentArticleTemplateCreate />;
};
