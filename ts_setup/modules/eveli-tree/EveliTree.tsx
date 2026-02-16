import React from 'react';
import { Box, Typography, List, IconButton, Badge, styled, Tooltip, Button } from '@mui/material';
import { TreeColors, TreeIcons } from './tree-theme';
import { TreeNode, mockTreeData, ContextMenuData, collapseAll, toggleNode, handleContextMenu, useEveliTree } from '@dxs-ts/eveli-tree-api';
import { useUtilityClasses, EveliTreeRoot } from './useUtilityClasses';
import { EveliTreeNode } from './eveli-tree-node';
import { EveliTreeNodeMenu } from './eveli-tree-node-menu';
import { EveliTreeSearch, EveliTreeSearchNoResults } from './eveli-tree-search';
import { filterTreeNodes } from './eveli-tree-search/search-helpers';

export const EveliTree: React.FC = () => {
  const { isDarkMode, setIsDarkMode, openAsset, searchExpanded, setSearchExpanded } = useEveliTree();
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

  const isAnyNodeExpanded = treeData.some(node => node.expanded || (node.children && node.children.some(child => child.expanded)));



  return (
    <EveliTreeRoot className={classes.root} isDarkTheme={isDarkMode}>
      <Box className={classes.title}>
        <Typography className={classes.titleText} mr={3}>Eveli Tree</Typography>
        <Box flexGrow={1} />
        <Tooltip title='Toggle search' arrow enterDelay={1000}>
          {isDarkMode ? (
            <StyledIconDark onClick={() => setSearchExpanded(!searchExpanded)}>
              <StyledBadgeDark
                badgeContent={searchExpanded ? <TreeIcons.Close sx={{ fontSize: '8px' }} /> : undefined}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              >
                <TreeIcons.Search sx={{ fontSize: '15px', transform: 'scaleX(-1)' }} />
              </StyledBadgeDark>
            </StyledIconDark>
          ) : (
            <StyledIconLight onClick={() => setSearchExpanded(!searchExpanded)}>
              <StyledBadgeLight
                badgeContent={searchExpanded ? <TreeIcons.Close sx={{ fontSize: '8px' }} /> : undefined}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              >
                <TreeIcons.Search sx={{ fontSize: '15px', transform: 'rotate(90deg)' }} />
              </StyledBadgeLight>
            </StyledIconLight>
          )}
        </Tooltip>
        <Tooltip title='New file' arrow enterDelay={1000}>
          {isDarkMode ? (
            <StyledIconDark>
              <StyledBadgeDark badgeContent={<TreeIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <TreeIcons.File sx={{ fontSize: '15px' }} />
              </StyledBadgeDark>
            </StyledIconDark>
          ) : (
            <StyledIconLight>
              <StyledBadgeLight badgeContent={<TreeIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <TreeIcons.File sx={{ fontSize: '15px' }} />
              </StyledBadgeLight>
            </StyledIconLight>
          )}
        </Tooltip>
        <Tooltip title='New folder' arrow enterDelay={1000}>
          {isDarkMode ? (
            <StyledIconDark>
              <StyledBadgeDark badgeContent={<TreeIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <TreeIcons.Folder sx={{ fontSize: '15px' }} />
              </StyledBadgeDark>
            </StyledIconDark>
          ) : (
            <StyledIconLight>
              <StyledBadgeLight badgeContent={<TreeIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <TreeIcons.Folder sx={{ fontSize: '15px' }} />
              </StyledBadgeLight>
            </StyledIconLight>
          )}

        </Tooltip>

        <Tooltip title='Collapse all' arrow enterDelay={1000}>
          {isDarkMode ? (
            <StyledIconDark
              onClick={() => collapseAll(treeData, setTreeData)}
              disabled={!isAnyNodeExpanded}
            >
              <TreeIcons.CollapseAll sx={{ fontSize: '15px' }} />
            </StyledIconDark>
          ) : (
            <StyledIconLight
              onClick={() => collapseAll(treeData, setTreeData)}
              disabled={!isAnyNodeExpanded}
            >
              <TreeIcons.CollapseAll sx={{ fontSize: '15px' }} />
            </StyledIconLight>
          )}
        </Tooltip>

        <Tooltip title='Toggle light/dark mode' arrow enterDelay={1000}>
          {isDarkMode ? (
            <StyledIconDark onClick={() => setIsDarkMode(!isDarkMode)}>
              <TreeIcons.LightMode sx={{ fontSize: '15px' }} />
            </StyledIconDark>
          ) : (
            <StyledIconLight onClick={() => setIsDarkMode(!isDarkMode)}>
              <TreeIcons.DarkMode sx={{ fontSize: '15px' }} />
            </StyledIconLight>
          )}
        </Tooltip>
      </Box>
      <EveliTreeSearch searchTerm={searchTerm} onSearchChange={setSearchTerm} isDarkMode={isDarkMode} open={searchExpanded} />

      {filteredTreeData.length === 0 ? <EveliTreeSearchNoResults /> :
        <List component='nav' disablePadding>
          {filteredTreeData.map((node) => (
            <EveliTreeNode
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
      <EveliTreeNodeMenu
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
  color: TreeColors.light.text,
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
    backgroundColor: TreeColors.light.text,
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