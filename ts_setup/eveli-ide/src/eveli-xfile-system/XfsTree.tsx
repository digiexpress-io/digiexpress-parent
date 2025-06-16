import React from 'react';

import { XfsTreeItem } from './XfsTreeItem';
import { Box } from '@mui/system';
import { useXfs } from './XfsContext';

export interface XfsTreeProps {

}


export const XfsTree: React.FC<XfsTreeProps> = ({ }) => {
  const { fs } = useXfs();
  return (
    <Box>
      {fs.nodes.map(file => <XfsTreeItem key={file.absolutePath}>{file}</XfsTreeItem>)}
    </Box>);
}
