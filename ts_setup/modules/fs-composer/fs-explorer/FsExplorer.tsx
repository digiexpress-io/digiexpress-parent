import React from 'react';
import { Box, Typography, List, IconButton, Badge, Tooltip } from '@mui/material';
import { FsIcons } from '../fs-theme';
import { FsNodeItem } from '../fs-node';
import { FsNodeMenu } from '../fs-node-menu';
import { FsSearch, FsSearchNoResults } from '../fs-search';
import { FsExplorerProps } from './FsExplorerProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsExplorerRoot } from './useUtilityClasses';

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
            <IconButton className={classes.iconDark} onClick={() => ownerState.setSearchExpanded(!ownerState.isSearchExpanded)}>
              <Badge
                className={classes.badgeDark}
                badgeContent={ownerState.isSearchExpanded ? <FsIcons.Close sx={{ fontSize: '8px' }} /> : undefined}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              >
                <FsIcons.Search sx={{ fontSize: '15px', transform: 'scaleX(-1)' }} />
              </Badge>
            </IconButton>
          ) : (
              <IconButton className={classes.iconLight} onClick={() => ownerState.setSearchExpanded(!ownerState.isSearchExpanded)}>
                <Badge
                  className={classes.badgeLight}
                  badgeContent={ownerState.isSearchExpanded ? <FsIcons.Close sx={{ fontSize: '8px' }} /> : undefined}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              >
                <FsIcons.Search sx={{ fontSize: '15px', transform: 'rotate(90deg)' }} />
                </Badge>
              </IconButton>
          )}
        </Tooltip>
        <Tooltip title='New file' arrow enterDelay={1000}>
          {ownerState.isDarkMode ? (
            <IconButton className={classes.iconDark}>
              <Badge className={classes.badgeDark} badgeContent={<FsIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FsIcons.File sx={{ fontSize: '15px' }} />
              </Badge>
            </IconButton>
          ) : (
              <IconButton className={classes.iconLight}>
                <Badge className={classes.badgeLight} badgeContent={<FsIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FsIcons.File sx={{ fontSize: '15px' }} />
                </Badge>
              </IconButton>
          )}
        </Tooltip>
        <Tooltip title='New folder' arrow enterDelay={1000}>
          {ownerState.isDarkMode ? (
            <IconButton className={classes.iconDark}>
              <Badge className={classes.badgeDark} badgeContent={<FsIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FsIcons.Folder sx={{ fontSize: '15px' }} />
              </Badge>
            </IconButton>
          ) : (
              <IconButton className={classes.iconLight}>
                <Badge className={classes.badgeLight} badgeContent={<FsIcons.Add sx={{ fontSize: '8px' }} />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                <FsIcons.Folder sx={{ fontSize: '15px' }} />
                </Badge>
              </IconButton>
          )}

        </Tooltip>

        <Tooltip title='Collapse all' arrow enterDelay={1000}>
          {ownerState.isDarkMode ? (
            <IconButton
              className={classes.iconDark}
              onClick={() => ownerState.collapseAll(ownerState.fsData, ownerState.setFsData)}
              disabled={!ownerState.isAnyNodeExpanded}
            >
              <FsIcons.CollapseAll sx={{ fontSize: '15px' }} />
            </IconButton>
          ) : (
              <IconButton
                className={classes.iconLight}
              onClick={() => ownerState.collapseAll(ownerState.fsData, ownerState.setFsData)}
              disabled={!ownerState.isAnyNodeExpanded}
            >
              <FsIcons.CollapseAll sx={{ fontSize: '15px' }} />
              </IconButton>
          )}
        </Tooltip>

        <Tooltip title='Toggle light/dark mode' arrow enterDelay={1000}>
          {ownerState.isDarkMode ? (
            <IconButton className={classes.iconDark} onClick={() => ownerState.setIsDarkMode(!ownerState.isDarkMode)}>
              <FsIcons.LightMode sx={{ fontSize: '15px' }} />
            </IconButton>
          ) : (
              <IconButton className={classes.iconLight} onClick={() => ownerState.setIsDarkMode(!ownerState.isDarkMode)}>
              <FsIcons.DarkMode sx={{ fontSize: '15px' }} />
              </IconButton>
          )}
        </Tooltip>
      </Box>
      <FsSearch
        searchTerm={ownerState.searchTerm}
        onSearchChange={ownerState.setSearchTerm}
        isDarkMode={ownerState.isDarkMode}
        open={ownerState.isSearchExpanded}
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
        open={ownerState.isContextMenuOpen}
        onClose={ownerState.onContextMenuClose}
        onExited={() => ownerState.setContextMenuData(undefined)}
      />
    </FsExplorerRoot>
  );
}