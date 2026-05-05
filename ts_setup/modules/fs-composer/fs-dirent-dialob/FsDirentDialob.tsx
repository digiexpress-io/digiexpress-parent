import React from 'react';
import { FsDirentDialobProps } from './FsDirentDialobProps';
import { FsDirentDialobCreate } from './FsDirentDialobCreate';
import { FsDirentDialobUpdate } from './FsDirentDialobUpdate';


export const FsDirentDialob: React.FC<FsDirentDialobProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentDialobUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentDialobCreate />;
};
