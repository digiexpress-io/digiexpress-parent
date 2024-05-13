import React from 'react';
import { Box, Typography } from '@mui/material';

import { XfsNode } from './XfsTypes';

export interface XfsTreeItemProps {
  children: XfsNode;
}

const PADDING_INCREMENT = 2;
function getPadding(currentNode: XfsNode) {
  const padding = (currentNode.depth - 1) * PADDING_INCREMENT;
  return padding;
}


export const XfsTreeItemFile: React.FC<XfsTreeItemProps> = ({ children: currentNode }) => {
  return (
    <Box display='flex' paddingLeft={getPadding(currentNode)}>
      <Box pl={2}><Typography variant='h5'>{currentNode.nodeName}</Typography></Box>
    </Box>);
}