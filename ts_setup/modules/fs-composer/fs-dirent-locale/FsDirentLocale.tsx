import React from 'react';
import { FsDirentLocaleProps } from './FsDirentLocaleProps';
import { FsDirentLocaleCreate } from './FsDirentLocaleCreate';
import { FsDirentLocaleUpdate } from './FsDirentLocaleUpdate';

export const FsDirentLocale: React.FC<FsDirentLocaleProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentLocaleUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentLocaleCreate />;
};
