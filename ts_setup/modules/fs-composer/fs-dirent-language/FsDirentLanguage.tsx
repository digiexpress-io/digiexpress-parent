import React from 'react';
import { FsDirentLanguageProps } from './FsDirentLanguageProps';
import { FsDirentLanguageCreate } from './FsDirentLanguageCreate';
import { FsDirentLanguageUpdate } from './FsDirentLanguageUpdate';

export const FsDirentLanguage: React.FC<FsDirentLanguageProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentLanguageUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentLanguageCreate />;
};
