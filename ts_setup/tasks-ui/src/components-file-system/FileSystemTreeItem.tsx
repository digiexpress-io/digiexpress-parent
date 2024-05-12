import React from 'react';
import { IconButton, Collapse, Box, Typography } from '@mui/material';

import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';


import { HdesFile, HdesFileSystem } from './FileSystemTypes';
import { useFileSystemTreeFocus } from './FileSystemTreeFocus';
import { useFileSystemExpander } from './FileSystemExpanderContext';
import { HierarchicalFileSystemNode, useFileSystem } from './FileSystemContext';

export interface FileSystemTreeItemProps {
  children: HierarchicalFileSystemNode;
}

const PADDING_INCREMENT = 2;


const File: React.FC<FileSystemTreeItemProps> = ({ children: currentNode }) => {
  const file = useFileSystemTreeItem(currentNode);
  const padding = (currentNode.depth - 1) * PADDING_INCREMENT;

  return (<>
    <Box pl={2}><Typography variant='h5'>{currentNode.nodeName}</Typography></Box>
  </>);
}

const Folder: React.FC<FileSystemTreeItemProps> = ({ children: currentNode }) => {
  return (<>
    <FileSystemTreeItemPanderButton node={currentNode} />
    <Box><Typography variant='h5'>{currentNode.nodeName}</Typography></Box>
  </>);
}

export const FileSystemTreeItem: React.FC<FileSystemTreeItemProps> = ({ children: currentNode }) => {
  const file = useFileSystemTreeItem(currentNode);
  const padding = (currentNode.depth - 1) * PADDING_INCREMENT;

  return (<>
    <Box display='flex' paddingLeft={padding}>
      {currentNode.file.fileType === 'FOLDER' ? <Folder>{currentNode}</Folder> : <File>{currentNode}</File>}
    </Box>

    {currentNode.file.fileType === 'FOLDER' && <FileSystemTreeItemCollpase node={currentNode}>
      {currentNode.children.map(childNode => <FileSystemTreeItem key={childNode.file.id}>{childNode}</FileSystemTreeItem>)}
    </FileSystemTreeItemCollpase>}
  </>);
}


const FileSystemTreeItemCollpase: React.FC<{ children: React.ReactNode, node: HierarchicalFileSystemNode }> = ({ children, node }) => {
  const { getExpanded } = useFileSystemExpander()
  const open = getExpanded(node.file.id);


  return (<Collapse in={open} timeout="auto" unmountOnExit sx={{ width: '100%' }}>
    {children}
  </Collapse>)
}

const FileSystemTreeItemPanderButton: React.FC<{ node: HierarchicalFileSystemNode }> = ({ node }) => {
  const { getExpanded, toggleExpanded } = useFileSystemExpander()
  const open = getExpanded(node.file.id);

  return (<IconButton
    size="small"
    onClick={() => toggleExpanded(node.file.id)}>
    {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
  </IconButton>);
}


export function useFileSystemTreeItem(file: HierarchicalFileSystemNode) {
  const { } = useFileSystemExpander();
  const { } = useFileSystemTreeFocus();

  return {}
}