import React from 'react';
import { IconButton, Collapse, Box, Typography } from '@mui/material';

import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';


import { HdesFile, HdesFileSystem } from './HdesFileSystemTypes';
import { useXfsTreeFocus } from './XfsTreeFocus';
import { useXfsExpander } from './XfsExpanderContext';
import { XfsNode } from './XfsTypes';

export interface XfsTreeItemProps {
  children: XfsNode;
}

const PADDING_INCREMENT = 2;


const File: React.FC<XfsTreeItemProps> = ({ children: currentNode }) => {
  const padding = (currentNode.depth - 1) * PADDING_INCREMENT;

  return (<>
    <Box pl={2}><Typography variant='h5'>{currentNode.nodeName}</Typography></Box>
  </>);
}

const Folder: React.FC<XfsTreeItemProps> = ({ children: currentNode }) => {
  return (<>
    <XfsTreeItemPanderButton node={currentNode} />
    <Box><Typography variant='h5'>{currentNode.nodeName}</Typography></Box>
  </>);
}

export const XfsTreeItem: React.FC<XfsTreeItemProps> = ({ children: currentNode }) => {
  const padding = (currentNode.depth - 1) * PADDING_INCREMENT;

  return (<>
    <Box display='flex' paddingLeft={padding}>
      {currentNode.file.fileType === 'FOLDER' ? <Folder>{currentNode}</Folder> : <File>{currentNode}</File>}
    </Box>

    {currentNode.file.fileType === 'FOLDER' && <XfsTreeItemCollpase node={currentNode}>
      {currentNode.children.map(childNode => <XfsTreeItem key={childNode.file.id}>{childNode}</XfsTreeItem>)}
    </XfsTreeItemCollpase>}
  </>);
}


const XfsTreeItemCollpase: React.FC<{ children: React.ReactNode, node: XfsNode }> = ({ children, node }) => {
  const { getExpanded } = useXfsExpander()
  const open = getExpanded(node.file.id);

  return (<Collapse in={open} timeout="auto" unmountOnExit sx={{ width: '100%' }}>
    {children}
  </Collapse>)
}

const XfsTreeItemPanderButton: React.FC<{ node: XfsNode }> = ({ node }) => {
  const { getExpanded, toggleExpanded } = useXfsExpander()
  const open = getExpanded(node.file.id);

  return (<IconButton
    size="small"
    onClick={() => toggleExpanded(node.file.id)}>
    {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
  </IconButton>);
}