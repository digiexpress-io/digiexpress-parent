import React from 'react';
import { FsDirentPhoneProps } from './FsDirentPhoneProps';
import { FsDirentPhoneCreate } from './FsDirentPhoneCreate';
import { FsDirentPhoneUpdate } from './FsDirentPhoneUpdate';


export const FsDirentPhone: React.FC<FsDirentPhoneProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentPhoneUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentPhoneCreate />;
};
