import React from 'react';
import { Box, Typography, List, IconButton, Badge, styled, Tooltip } from '@mui/material';
import {
  UnfoldLessOutlined as CollapseAllIcon,
  LightModeOutlined as LightModeIcon,
  DarkModeOutlined as DarkModeIcon,
  InsertDriveFileOutlined as FileIcon,
  FolderOutlined as FolderIcon,
  Add as AddIcon
} from '@mui/icons-material';
import { TreeNode, mockTreeData, ContextMenuData, collapseAll, toggleNode, handleContextMenu } from '../eveli-tree-api';
import { useUtilityClasses, EveliTreeRoot } from './useUtilityClasses';
import { EveliTreeItem } from './eveli-tree-item';
import { EveliTreeItemMenu } from './eveli-tree-item-menu';


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
        <Tooltip title='New file' arrow enterDelay={1000}>
          <StyledIcon isDarkTheme={isDarkTheme}>
            <StyledBadge isDarkTheme={isDarkTheme} badgeContent={<AddIcon sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FileIcon sx={{ fontSize: '15px' }} />
            </StyledBadge>
          </StyledIcon>
        </Tooltip>
        <Tooltip title='New folder' arrow enterDelay={1000}>
          <StyledIcon isDarkTheme={isDarkTheme}>
            <StyledBadge isDarkTheme={isDarkTheme} badgeContent={<AddIcon sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FolderIcon sx={{ fontSize: '15px' }} />
            </StyledBadge>
          </StyledIcon>
        </Tooltip>

        <Tooltip title='Collapse all' arrow enterDelay={1000}>
          <StyledIcon isDarkTheme={isDarkTheme} onClick={() => collapseAll(treeData, setTreeData)}>
            <CollapseAllIcon sx={{ fontSize: '15px' }} />
          </StyledIcon>
        </Tooltip>

        <Tooltip title='Toggle light/dark mode' arrow enterDelay={1000}>
          <StyledIcon isDarkTheme={isDarkTheme} onClick={() => setIsDarkTheme(!isDarkTheme)}>
            {isDarkTheme ? <LightModeIcon sx={{ fontSize: '15px' }} /> : <DarkModeIcon sx={{ fontSize: '15px' }} />}
          </StyledIcon>
        </Tooltip>
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

const StyledIcon = styled(IconButton)<{ isDarkTheme: boolean }>(({ isDarkTheme }) => ({
  size: 'small',
  color: isDarkTheme ? '#cccccc' : '#666666',
}));


const StyledBadge = styled(Badge)<{ isDarkTheme: boolean }>(({ isDarkTheme }) => ({
  '& .MuiBadge-badge': {
    backgroundColor: isDarkTheme ? '#cccccc' : '#666666',
    color: isDarkTheme ? '#2d2d30' : 'white',
    height: '10px',
    width: '10px',
    minWidth: '10px',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '2px',
    right: '2px',
    bottom: '2px',
  },
}));