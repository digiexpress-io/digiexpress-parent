import React from 'react';
import { FsDirentDecisionTableProps } from './FsDirentDecisionTableProps';
import { FsDirentDecisionTableCreate } from './FsDirentDecisionTableCreate';
import { FsDirentDecisionTableUpdate } from './FsDirentDecisionTableUpdate';


export const FsDirentDecisionTable: React.FC<FsDirentDecisionTableProps> = ({ tab }) => {
  if (tab.type === 'edit') {
    return <FsDirentDecisionTableUpdate direntId={tab.dirent.id} />;
  }
  return <FsDirentDecisionTableCreate />;
};
