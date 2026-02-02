import React from 'react';
import { Box, Typography, List, IconButton } from '@mui/material';
import {
  UnfoldLess as CollapseAllIcon,
  LightMode as LightModeIcon,
  DarkMode as DarkModeIcon
} from '@mui/icons-material';
import { TreeNode, mockTreeData, ContextMenuData, collapseAll, toggleNode, handleContextMenu } from '../eveli-tree-api';
import { useUtilityClasses, EveliTreeRoot } from './useUtilityClasses';
import { EveliTreeItemMenu } from './EveliTreeItemMenu';
import { EveliTreeItem } from './eveli-tree-item';



export const EveliTree: React.FC = () => {
  const [isDarkTheme, setIsDarkTheme] = React.useState(false);
  const classes = useUtilityClasses(isDarkTheme);
  const [treeData, setTreeData] = React.useState<TreeNode[]>(mockTreeData);
  const [contextMenuOpen, setContextMenuOpen] = React.useState(false);
  const [contextMenuData, setContextMenuData] = React.useState<ContextMenuData | undefined>();


  function handleContextMenuClose() {
    setContextMenuOpen(false);
  }

  return (
    <EveliTreeRoot className={classes.root} isDarkTheme={isDarkTheme}>
      <Box className={classes.title}>
        <Typography className={classes.titleText} mr={3}>Eveli Tree</Typography>
        <Box flexGrow={1} />
        <IconButton size='small' onClick={() => collapseAll(treeData, setTreeData)}
          sx={{
            color: isDarkTheme ? '#cccccc' : '#666666',
            '&:hover': {
              backgroundColor: isDarkTheme ? '#3c3c3c' : '#e0e0e0',
            },
          }}
        >
          <CollapseAllIcon fontSize='small' />
        </IconButton>
        <IconButton size='small' onClick={() => setIsDarkTheme(!isDarkTheme)}
          sx={{
            color: isDarkTheme ? '#cccccc' : '#666666',
            '&:hover': {
              backgroundColor: isDarkTheme ? '#3c3c3c' : '#e0e0e0',
            },
          }}
        >
          {isDarkTheme ? <LightModeIcon fontSize='small' /> : <DarkModeIcon fontSize='small' />}
        </IconButton>
      </Box>
      <List component='nav' disablePadding>
        {treeData.map((node) => (
          <EveliTreeItem
            key={node.id}
            node={node}
            level={0}
            onToggle={(nodeId) => toggleNode(nodeId, treeData, setTreeData)}
            onContextMenu={(event, node) => handleContextMenu(event, node, setContextMenuData, setContextMenuOpen)}
            isDarkTheme={isDarkTheme}
          />
        ))}
      </List>

      <EveliTreeItemMenu
        node={contextMenuData?.node || undefined}
        anchorPosition={contextMenuData?.anchorPosition || undefined}
        open={contextMenuOpen}
        onClose={handleContextMenuClose}
        onExited={() => setContextMenuData(undefined)}
      />
    </EveliTreeRoot>
  );
}