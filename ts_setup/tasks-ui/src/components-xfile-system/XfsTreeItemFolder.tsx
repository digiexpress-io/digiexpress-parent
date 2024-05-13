import React from 'react';
import { IconButton, Collapse, Box, Typography } from '@mui/material';

import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';

import { XfsNode } from './XfsTypes';
import { XfsTreeItem } from './XfsTreeItem';
import { useXfsTreeFocus } from './XfsTreeFocus';
import { XFolderIcon } from './XFolderIcon';


const PADDING_INCREMENT = 2;

function getPadding(currentNode: XfsNode) {
  const padding = (currentNode.depth - 1) * PADDING_INCREMENT;
  return padding;
}

const ExpanderIcon: React.FC<{ children: boolean }> = React.memo(({ children: open }) => {
  return open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />;
})


export const XfsTreeItemFolder: React.FC<{ children: XfsNode, expanded: boolean }> = ({ children: currentNode, expanded }) => {

  return (<>
    <Box sx={{ display: 'flex', cursor: 'pointer' }} paddingLeft={getPadding(currentNode)}>
      <IconButton size="small" >
        <ExpanderIcon>{expanded}</ExpanderIcon>
      </IconButton>
      <XFolderIcon />
      <Box sx={{ display: 'inline-flex', alignItems: 'center' }}>
        <Typography variant='h5'>{currentNode.nodeName}</Typography>
      </Box>
    </Box>
  </>);
}
