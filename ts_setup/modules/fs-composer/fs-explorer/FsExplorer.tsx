import React from 'react';
import { Box, Typography, List, IconButton, Badge, styled, Tooltip } from '@mui/material';
import { FsColors, FsIcons } from '../fs-theme';
import { useUtilityClasses, FsExplorerRoot } from './useUtilityClasses';
import { FsNodeItem } from '../fs-node';
import { FsNodeMenu } from '../fs-node-menu';
import { FsSearch, FsSearchNoResults } from '../fs-search';
import { FsExplorerProps } from './FsExplorerProps';
import { useOwnerState } from './useOwnerState';

export const FsExplorer: React.FC<FsExplorerProps> = (props) => {
  const classes = useUtilityClasses();
  const ownerState = useOwnerState(props);


  return (
    <FsExplorerRoot className={classes.root} isDarkTheme={ownerState.isDarkMode}>
      <Box className={classes.title}>
        <Typography className={classes.titleText} mr={3}>File System Composer</Typography>
        <Box flexGrow={1} />
        <Tooltip title='Toggle search' arrow enterDelay={1000}>
          {ownerState.isDarkMode ? (
            <StyledIconDark onClick={() => ownerState.setSearchExpanded(!ownerState.searchExpanded)}>
              <StyledBadgeDark
                badgeContent={ownerState.searchExpanded ? <FsIcons.Close sx={{ fontSize: '8px' }} /> : undefined}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              >
                <FsIcons.Search sx={{ fontSize: '15px', transform: 'scaleX(-1)' }} />
              </StyledBadgeDark>
            </StyledIconDark>
          ) : (
            <StyledIconLight onClick={() => ownerState.setSearchExpanded(!ownerState.searchExpanded)}>
              <StyledBadgeLight
                badgeContent={ownerState.searchExpanded ? <FsIcons.Close sx={{ fontSize: '8px' }} /> : undefined}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              >
                <FsIcons.Search sx={{ fontSize: '15px', transform: 'rotate(90deg)' }} />
              </StyledBadgeLight>
            </StyledIconLight>
          )}
        </Tooltip>
        <Tooltip title='New file' arrow enterDelay={1000}>
          {ownerState.isDarkMode ? (
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
          {ownerState.isDarkMode ? (
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
          {ownerState.isDarkMode ? (
            <StyledIconDark
              onClick={() => ownerState.collapseAll(ownerState.fsData, ownerState.setFsData)}
              disabled={!ownerState.isAnyNodeExpanded}
            >
              <FsIcons.CollapseAll sx={{ fontSize: '15px' }} />
            </StyledIconDark>
          ) : (
            <StyledIconLight
              onClick={() => ownerState.collapseAll(ownerState.fsData, ownerState.setFsData)}
              disabled={!ownerState.isAnyNodeExpanded}
            >
              <FsIcons.CollapseAll sx={{ fontSize: '15px' }} />
            </StyledIconLight>
          )}
        </Tooltip>

        <Tooltip title='Toggle light/dark mode' arrow enterDelay={1000}>
          {ownerState.isDarkMode ? (
            <StyledIconDark onClick={() => ownerState.setIsDarkMode(!ownerState.isDarkMode)}>
              <FsIcons.LightMode sx={{ fontSize: '15px' }} />
            </StyledIconDark>
          ) : (
            <StyledIconLight onClick={() => ownerState.setIsDarkMode(!ownerState.isDarkMode)}>
              <FsIcons.DarkMode sx={{ fontSize: '15px' }} />
            </StyledIconLight>
          )}
        </Tooltip>
      </Box>
      <FsSearch
        searchTerm={ownerState.searchTerm}
        onSearchChange={ownerState.setSearchTerm}
        isDarkMode={ownerState.isDarkMode}
        open={ownerState.searchExpanded}
        visibleFilters={ownerState.filters}
        onFiltersChange={ownerState.setFilters}
      />

      {ownerState.filteredTreeData.length === 0 ? <FsSearchNoResults /> :
        <List component='nav' disablePadding>
          {ownerState.filteredTreeData.map((node) => (
            <FsNodeItem
              key={node.id}
              node={node}
              level={0}
              onToggle={(nodeId) => ownerState.toggleNode(nodeId, ownerState.fsData, ownerState.setFsData)}
              onContextMenu={(event, node) => ownerState.onContextMenu(event, node, ownerState.setContextMenuData, ownerState.setContextMenuOpen)}
              onDoubleClick={ownerState.onDoubleClick}
              isDarkTheme={ownerState.isDarkMode}
              searchTerm={ownerState.searchTerm}
            />
          ))}
        </List>
      }
      <FsNodeMenu
        node={ownerState.contextMenuData?.node || undefined}
        anchorPosition={ownerState.contextMenuData?.anchorPosition || undefined}
        open={ownerState.contextMenuOpen}
        onClose={ownerState.onContextMenuClose}
        onExited={() => ownerState.setContextMenuData(undefined)}
      />
    </FsExplorerRoot>
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