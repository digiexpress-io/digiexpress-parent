import React from 'react';
import { Box, Typography } from '@mui/material';

import { XfsNode } from './XfsTypes';
import { XFileIcon } from './XFileIcon';
import { useXfsTreeFocus } from './XfsTreeFocus';

export interface XfsTreeItemProps {
  children: XfsNode;
}

const PADDING_INCREMENT = 2;
function getPadding(currentNode: XfsNode) {
  const padding = (currentNode.depth - 1) * PADDING_INCREMENT;
  return padding;
}


export const XfsTreeItemFile: React.FC<XfsTreeItemProps> = ({ children: currentNode }) => {
  const { setTreeFocusOn, } = useXfsTreeFocus();
  function handleExpander() {
    setTreeFocusOn(currentNode.file.id);
    
  }
  return (
    <Box sx={{display: 'flex', cursor: 'pointer'}} paddingLeft={getPadding(currentNode)} onClick={handleExpander}>
      <Box sx={{ paddingLeft: 4}}><XFileIcon /></Box>
      <Box sx={{ display: 'inline-flex', alignItems: 'center' }}>
        <Typography variant='h5'>{currentNode.nodeName}</Typography>
      </Box>
    </Box>);
}