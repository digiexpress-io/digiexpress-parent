import React from 'react';
import { useFileSystem } from './FileSystemContext';
import { FileSystemTreeItem } from './FileSystemTreeItem';
import { Box } from '@mui/system';

export interface FileSystemTreeProps {
  
}


export const FileSystemTree: React.FC<FileSystemTreeProps> = ({  }) => {
  const { fs } = useFileSystem();
  return (<Box>
    {fs.nodes.map(file => <FileSystemTreeItem key={file.absolutePath}>{file}</FileSystemTreeItem>)}
  </Box>);
}
