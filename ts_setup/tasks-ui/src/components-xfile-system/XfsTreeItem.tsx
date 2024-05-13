import React from 'react';
import { XfsNode } from './XfsTypes';
import { XfsTreeItemFolder } from './XfsTreeItemFolder';
import { XfsTreeItemFile } from './XfsTreeItemFile';
import { useXfsTreeFocus } from './XfsTreeFocus';
import { Box, Collapse } from '@mui/material';
import { grey_light_2 } from 'components-colors';



export const XfsTreeItem: React.FC<{children: XfsNode}> = ({ children: currentNode }) => {
  const [expanded, setExpanded] = React.useState(false);
  const { setTreeFocusOn } = useXfsTreeFocus();

  function handleExpander() {
    setTreeFocusOn(currentNode.file.id);
    setExpanded(prev => !prev);
  }

  const focus = useXfsTreeFocus();
  const isFocused = focus.getTreeFocusOn(currentNode.file.id);
  const backgroundColor = isFocused ? grey_light_2 : undefined;

  const item = currentNode.file.fileType === 'FOLDER' ? 
    (<XfsTreeItemFolder expanded={expanded}>{currentNode}</XfsTreeItemFolder>) : 
    (<XfsTreeItemFile>{currentNode}</XfsTreeItemFile>);
    
  return (
    <>
    <Box sx={{backgroundColor}} onClick={handleExpander}>{item}</Box>

    <Collapse in={expanded} timeout="auto" unmountOnExit sx={{ width: '100%' }}>
      {currentNode.children.map(childNode => <XfsTreeItem key={childNode.file.id}>{childNode}</XfsTreeItem>)}
    </Collapse>
    </>
  );
}
