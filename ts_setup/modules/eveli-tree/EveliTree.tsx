import React from 'react';
import { Box, Typography, List, IconButton, Badge, styled, Tooltip } from '@mui/material';
import { TreeColors } from './tree-theme';
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
          {isDarkTheme ? (
            <StyledIconDark>
              <StyledBadgeDark badgeContent={<AddIcon sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FileIcon sx={{ fontSize: '15px' }} />
              </StyledBadgeDark>
            </StyledIconDark>
          ) : (
            <StyledIconLight>
              <StyledBadgeLight badgeContent={<AddIcon sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FileIcon sx={{ fontSize: '15px' }} />
              </StyledBadgeLight>
            </StyledIconLight>
          )}
        </Tooltip>
        <Tooltip title='New folder' arrow enterDelay={1000}>
          {isDarkTheme ? (
            <StyledIconDark>
              <StyledBadgeDark badgeContent={<AddIcon sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FolderIcon sx={{ fontSize: '15px' }} />
              </StyledBadgeDark>
            </StyledIconDark>
          ) : (
            <StyledIconLight>
              <StyledBadgeLight badgeContent={<AddIcon sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FolderIcon sx={{ fontSize: '15px' }} />
              </StyledBadgeLight>
            </StyledIconLight>
          )}
        </Tooltip>

        <Tooltip title='Collapse all' arrow enterDelay={1000}>
          {isDarkTheme ? (
            <StyledIconDark onClick={() => collapseAll(treeData, setTreeData)}>
              <CollapseAllIcon sx={{ fontSize: '15px' }} />
            </StyledIconDark>
          ) : (
            <StyledIconLight onClick={() => collapseAll(treeData, setTreeData)}>
              <CollapseAllIcon sx={{ fontSize: '15px' }} />
            </StyledIconLight>
          )}
        </Tooltip>

        <Tooltip title='Toggle light/dark mode' arrow enterDelay={1000}>
          {isDarkTheme ? (
            <StyledIconDark onClick={() => setIsDarkTheme(!isDarkTheme)}>
              <LightModeIcon sx={{ fontSize: '15px' }} />
            </StyledIconDark>
          ) : (
            <StyledIconLight onClick={() => setIsDarkTheme(!isDarkTheme)}>
              <DarkModeIcon sx={{ fontSize: '15px' }} />
            </StyledIconLight>
          )}
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

const StyledIconDark = styled(IconButton)(() => ({
  size: 'small',
  color: TreeColors.dark.text,
}));

const StyledIconLight = styled(IconButton)(() => ({
  size: 'small',
  color: TreeColors.light.textSecondary,
}));

const StyledBadgeDark = styled(Badge)(() => ({
  '& .MuiBadge-badge': {
    backgroundColor: TreeColors.dark.text,
    color: TreeColors.dark.surface,
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

const StyledBadgeLight = styled(Badge)(() => ({
  '& .MuiBadge-badge': {
    backgroundColor: TreeColors.light.textSecondary,
    color: TreeColors.light.background,
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