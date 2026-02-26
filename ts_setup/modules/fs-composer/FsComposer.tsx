import React from 'react';
import { Box, Typography, List, IconButton, Badge, styled, Tooltip } from '@mui/material';
import { FsColors, FsIcons } from './fs-theme';
import { FsNode, mockFsData, FsContextMenuData, collapseAll, toggleNode, handleContextMenu, useFs } from '@dxs-ts/fs-api';
import { useUtilityClasses, FsComposerRoot } from './useUtilityClasses';
import { FsNodeItem } from './fs-node';
import { FsNodeMenu } from './fs-node-menu';
import { FsSearch, FsSearchNoResults } from './fs-search';
import { filterTreeNodes, FilterData } from './fs-search/search-helpers';

export const FsComposer: React.FC = () => {
  const { isDarkMode, setIsDarkMode, openAsset, searchExpanded, setSearchExpanded } = useFs();
  const classes = useUtilityClasses();
  const [fsData, setFsData] = React.useState<FsNode[]>(mockFsData);
  const [contextMenuOpen, setContextMenuOpen] = React.useState(false);
  const [contextMenuData, setContextMenuData] = React.useState<FsContextMenuData | undefined>();
  const [searchTerm, setSearchTerm] = React.useState('');
  const [filters, setFilters] = React.useState<FilterData[]>([]);

  const filteredTreeData = React.useMemo(() => {
    return filterTreeNodes(fsData, searchTerm, filters)
  }, [fsData, searchTerm, filters])

  function handleContextMenuClose() {
    setContextMenuOpen(false);
  }

  function handleDoubleClick(node: FsNode, pathToTopParent: string) {
    openAsset(node, pathToTopParent);
  }

  const isAnyNodeExpanded = fsData.some(node => node.expanded || (node.children && node.children.some(child => child.expanded)));



  return (
    <FsComposerRoot className={classes.root} isDarkTheme={isDarkMode}>
      <Box className={classes.title}>
        <Typography className={classes.titleText} mr={3}>File System Composer</Typography>
        <Box flexGrow={1} />
        <Tooltip title='Toggle search' arrow enterDelay={1000}>
          {isDarkMode ? (
            <StyledIconDark onClick={() => setSearchExpanded(!searchExpanded)}>
              <StyledBadgeDark
                badgeContent={searchExpanded ? <FsIcons.Close sx={{ fontSize: '8px' }} /> : undefined}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              >
                <FsIcons.Search sx={{ fontSize: '15px', transform: 'scaleX(-1)' }} />
              </StyledBadgeDark>
            </StyledIconDark>
          ) : (
            <StyledIconLight onClick={() => setSearchExpanded(!searchExpanded)}>
              <StyledBadgeLight
                badgeContent={searchExpanded ? <FsIcons.Close sx={{ fontSize: '8px' }} /> : undefined}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              >
                <FsIcons.Search sx={{ fontSize: '15px', transform: 'rotate(90deg)' }} />
              </StyledBadgeLight>
            </StyledIconLight>
          )}
        </Tooltip>
        <Tooltip title='New file' arrow enterDelay={1000}>
          {isDarkMode ? (
            <StyledIconDark>
              <StyledBadgeDark badgeContent={<FsIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FsIcons.File sx={{ fontSize: '15px' }} />
              </StyledBadgeDark>
            </StyledIconDark>
          ) : (
            <StyledIconLight>
              <StyledBadgeLight badgeContent={<FsIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FsIcons.File sx={{ fontSize: '15px' }} />
              </StyledBadgeLight>
            </StyledIconLight>
          )}
        </Tooltip>
        <Tooltip title='New folder' arrow enterDelay={1000}>
          {isDarkMode ? (
            <StyledIconDark>
              <StyledBadgeDark badgeContent={<FsIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FsIcons.Folder sx={{ fontSize: '15px' }} />
              </StyledBadgeDark>
            </StyledIconDark>
          ) : (
            <StyledIconLight>
              <StyledBadgeLight badgeContent={<FsIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FsIcons.Folder sx={{ fontSize: '15px' }} />
              </StyledBadgeLight>
            </StyledIconLight>
          )}

        </Tooltip>

        <Tooltip title='Collapse all' arrow enterDelay={1000}>
          {isDarkMode ? (
            <StyledIconDark
              onClick={() => collapseAll(fsData, setFsData)}
              disabled={!isAnyNodeExpanded}
            >
              <FsIcons.CollapseAll sx={{ fontSize: '15px' }} />
            </StyledIconDark>
          ) : (
            <StyledIconLight
              onClick={() => collapseAll(fsData, setFsData)}
              disabled={!isAnyNodeExpanded}
            >
              <FsIcons.CollapseAll sx={{ fontSize: '15px' }} />
            </StyledIconLight>
          )}
        </Tooltip>

        <Tooltip title='Toggle light/dark mode' arrow enterDelay={1000}>
          {isDarkMode ? (
            <StyledIconDark onClick={() => setIsDarkMode(!isDarkMode)}>
              <FsIcons.LightMode sx={{ fontSize: '15px' }} />
            </StyledIconDark>
          ) : (
            <StyledIconLight onClick={() => setIsDarkMode(!isDarkMode)}>
              <FsIcons.DarkMode sx={{ fontSize: '15px' }} />
            </StyledIconLight>
          )}
        </Tooltip>
      </Box>
      <FsSearch
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        isDarkMode={isDarkMode}
        open={searchExpanded}
        visibleFilters={filters}
        onFiltersChange={setFilters}
      />

      {filteredTreeData.length === 0 ? <FsSearchNoResults /> :
        <List component='nav' disablePadding>
          {filteredTreeData.map((node) => (
            <FsNodeItem
              key={node.id}
              node={node}
              level={0}
              onToggle={(nodeId) => toggleNode(nodeId, fsData, setFsData)}
              onContextMenu={(event, node) => handleContextMenu(event, node, setContextMenuData, setContextMenuOpen)}
              onDoubleClick={handleDoubleClick}
              isDarkTheme={isDarkMode}
              searchTerm={searchTerm}
            />
          ))}
        </List>
      }
      <FsNodeMenu
        node={contextMenuData?.node || undefined}
        anchorPosition={contextMenuData?.anchorPosition || undefined}
        open={contextMenuOpen}
        onClose={handleContextMenuClose}
        onExited={() => setContextMenuData(undefined)}
      />
    </FsComposerRoot>
  );
}

const StyledIconDark = styled(IconButton)(() => ({
  size: 'small',
  color: FsColors.dark.text,
}));

const StyledIconLight = styled(IconButton)(() => ({
  size: 'small',
  color: FsColors.light.text,
}));

const StyledBadgeDark = styled(Badge)(() => ({
  '& .MuiBadge-badge': {
    backgroundColor: FsColors.dark.text,
    color: FsColors.dark.surface,
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
    backgroundColor: FsColors.light.text,
    color: FsColors.light.background,
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