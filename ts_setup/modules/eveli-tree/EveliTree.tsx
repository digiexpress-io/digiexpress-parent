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
import { TreeNode, mockTreeData, ContextMenuData, collapseAll, toggleNode, handleContextMenu, useEveliTree } from '../eveli-tree-api';
import { useUtilityClasses, EveliTreeRoot } from './useUtilityClasses';
import { EveliTreeItem } from './eveli-tree-item';
import { EveliTreeItemMenu } from './eveli-tree-item-menu';
import { EveliTreeSearch, EveliTreeSearchNoResults } from './eveli-tree-search';
import { filterTreeNodes } from './eveli-tree-search/search-helpers';

export const EveliTree: React.FC = () => {
  const { isDarkMode, setIsDarkMode, openAsset } = useEveliTree();
  const classes = useUtilityClasses();
  const [treeData, setTreeData] = React.useState<TreeNode[]>(mockTreeData);
  const [contextMenuOpen, setContextMenuOpen] = React.useState(false);
  const [contextMenuData, setContextMenuData] = React.useState<ContextMenuData | undefined>();
  const [searchTerm, setSearchTerm] = React.useState('');

  const filteredTreeData = React.useMemo(() => {
    return filterTreeNodes(treeData, searchTerm)
  }, [treeData, searchTerm])

  function handleContextMenuClose() {
    setContextMenuOpen(false);
  }

  function handleDoubleClick(node: TreeNode, pathToTopParent: string) {
    openAsset(node, pathToTopParent);
  }

  return (
    <EveliTreeRoot className={classes.root} isDarkTheme={isDarkMode}>
      <Box className={classes.title}>
        <Typography className={classes.titleText} mr={3}>Eveli Tree</Typography>
        <Box flexGrow={1} />
        <Tooltip title='New file' arrow enterDelay={1000}>
          {isDarkMode ? (
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
          {isDarkMode ? (
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
          {isDarkMode ? (
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
          {isDarkMode ? (
            <StyledIconDark onClick={() => setIsDarkMode(!isDarkMode)}>
              <LightModeIcon sx={{ fontSize: '15px' }} />
            </StyledIconDark>
          ) : (
            <StyledIconLight onClick={() => setIsDarkMode(!isDarkMode)}>
              <DarkModeIcon sx={{ fontSize: '15px' }} />
            </StyledIconLight>
          )}
        </Tooltip>
      </Box>
      <EveliTreeSearch searchTerm={searchTerm} onSearchChange={setSearchTerm} />

      {filteredTreeData.length === 0 ? <EveliTreeSearchNoResults /> :
        <List component='nav' disablePadding>
          {filteredTreeData.map((node) => (
            <EveliTreeItem
              key={node.id}
              node={node}
              level={0}
              onToggle={(nodeId) => toggleNode(nodeId, treeData, setTreeData)}
              onContextMenu={(event, node) => handleContextMenu(event, node, setContextMenuData, setContextMenuOpen)}
              onDoubleClick={handleDoubleClick}
              isDarkTheme={isDarkMode}
              searchTerm={searchTerm}
            />
          ))}
        </List>
      }
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