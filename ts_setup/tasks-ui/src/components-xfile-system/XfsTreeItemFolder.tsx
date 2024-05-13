import React from 'react';
import { IconButton, Collapse, Box, Typography } from '@mui/material';

import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';

import { useXfsExpander } from './XfsExpanderContext';
import { XfsNode } from './XfsTypes';
import { XfsTreeItem } from './XfsTreeItem';


const PADDING_INCREMENT = 2;

function getPadding(currentNode: XfsNode) {
  const padding = (currentNode.depth - 1) * PADDING_INCREMENT;
  return padding;
}

const ExpanderIcon: React.FC<{ children: boolean }> = React.memo(({ children: open }) => {
  return open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />;
})


export const XfsTreeItemFolder: React.FC<{ children: XfsNode }> = ({ children: currentNode }) => {
  const { getExpanded, toggleExpanded } = useXfsExpander()
  const open = getExpanded(currentNode.file.id);

  function handleExpander() {
    toggleExpanded(currentNode.file.id);
  }

  return (<>
    <Box display='flex' paddingLeft={getPadding(currentNode)}>
      <IconButton size="small" onClick={handleExpander}>
        <ExpanderIcon>{open}</ExpanderIcon>
      </IconButton>
      <Box><Typography variant='h5'>{currentNode.nodeName}</Typography></Box>
    </Box>

    <Collapse in={open} timeout="auto" unmountOnExit sx={{ width: '100%' }}>
      {currentNode.children.map(childNode => <XfsTreeItem key={childNode.file.id}>{childNode}</XfsTreeItem>)}
    </Collapse>
  </>);
}
