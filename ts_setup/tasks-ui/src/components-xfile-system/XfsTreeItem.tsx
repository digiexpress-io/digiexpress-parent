import React from 'react';
import { XfsNode } from './XfsTypes';
import { XfsTreeItemFolder } from './XfsTreeItemFolder';
import { XfsTreeItemFile } from './XfsTreeItemFile';



export const XfsTreeItem: React.FC<{children: XfsNode}> = ({ children: currentNode }) => {
  if (currentNode.file.fileType === 'FOLDER') {
    return (<XfsTreeItemFolder>{currentNode}</XfsTreeItemFolder>);
  }
  return (<XfsTreeItemFile>{currentNode}</XfsTreeItemFile>);
}
