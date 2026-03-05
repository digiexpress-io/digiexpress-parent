import React from 'react';
import { Box, Typography, List, IconButton, Badge } from '@mui/material';
import { FsIcons, FsIcon } from '../fs-theme';
import { FsDirent } from '../fs-dirent';
import { FsDirentMenu } from '../fs-dirent-menu';
import { FsSearch } from '../fs-search';
import { FsExplorerNoSearchResults } from './FsExplorerNoSearchResults';
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
        {ownerState.isDarkMode ? (
          <IconButton className={classes.iconDark} onClick={() => ownerState.setSearchExpanded(!ownerState.isSearchExpanded)}>
            <Badge className={classes.badgeDark}
              badgeContent={ownerState.isSearchExpanded ? <FsIcon icon={FsIcons.Close} xsmall /> : undefined}
              anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            >
              <FsIcon icon={FsIcons.Search} small tooltip='Toggle search' />
            </Badge>
          </IconButton>
        ) : (
          <IconButton className={classes.iconLight} onClick={() => ownerState.setSearchExpanded(!ownerState.isSearchExpanded)}>
              <Badge className={classes.badgeLight}
                badgeContent={ownerState.isSearchExpanded ? <FsIcon icon={FsIcons.Close} xsmall /> : undefined}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              >
              <FsIcon icon={FsIcons.Search} small tooltip='Toggle search' />
            </Badge>
          </IconButton>
        )}
        {ownerState.isDarkMode ? (
          <IconButton className={classes.iconDark}>
            <Badge className={classes.badgeDark} badgeContent={<FsIcon icon={FsIcons.Add} xsmall />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FsIcon icon={FsIcons.File} small tooltip='New file' />
            </Badge>
          </IconButton>
        ) : (
          <IconButton className={classes.iconLight}>
            <Badge className={classes.badgeLight} badgeContent={<FsIcon icon={FsIcons.Add} xsmall />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FsIcon icon={FsIcons.File} small tooltip='New file' />
            </Badge>
          </IconButton>
        )}
        {ownerState.isDarkMode ? (
          <IconButton className={classes.iconDark}>
            <Badge className={classes.badgeDark} badgeContent={<FsIcon icon={FsIcons.Add} xsmall />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FsIcon icon={FsIcons.Folder} small tooltip='New folder' />
            </Badge>
          </IconButton>
        ) : (
          <IconButton className={classes.iconLight}>
            <Badge className={classes.badgeLight} badgeContent={<FsIcon icon={FsIcons.Add} xsmall />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FsIcon icon={FsIcons.Folder} small tooltip='New folder' />
            </Badge>
          </IconButton>
        )}

        {ownerState.isDarkMode ? (
          <IconButton className={classes.iconDark}
            onClick={() => ownerState.collapseAll(ownerState.fsData, ownerState.setFsData)}
            disabled={!ownerState.isAnyNodeExpanded}
          >
            <FsIcon icon={FsIcons.CollapseAll} small tooltip='Collapse all' />
          </IconButton>
        ) : (
            <IconButton className={classes.iconLight}
              onClick={() => ownerState.collapseAll(ownerState.fsData, ownerState.setFsData)}
              disabled={!ownerState.isAnyNodeExpanded}
            >
            <FsIcon icon={FsIcons.CollapseAll} small tooltip='Collapse all' />
          </IconButton>
        )}

        {ownerState.isDarkMode ? (
          <IconButton className={classes.iconDark} onClick={() => ownerState.setIsDarkMode(!ownerState.isDarkMode)}>
            <FsIcon icon={FsIcons.LightMode} small tooltip='Toggle light/dark mode' />
          </IconButton>
        ) : (
          <IconButton className={classes.iconLight} onClick={() => ownerState.setIsDarkMode(!ownerState.isDarkMode)}>
            <FsIcon icon={FsIcons.LightMode} small tooltip='Toggle light/dark mode' />
          </IconButton>
        )}
      </Box>
      <FsSearch
        searchTerm={ownerState.searchTerm}
        onSearchChange={ownerState.setSearchTerm}
        open={ownerState.isSearchExpanded}
        visibleFilters={ownerState.filters}
        onFiltersChange={ownerState.setFilters}
      />

      {ownerState.filteredTreeData.length === 0 ? <FsExplorerNoSearchResults /> :
        <List component='nav' disablePadding>
          {ownerState.filteredTreeData.map((node) => (
            <FsDirent
              key={node.id}
              node={node}
              level={0}
              onToggle={(nodeId) => ownerState.toggleNode(nodeId, ownerState.fsData, ownerState.setFsData)}
              onContextMenu={(event, node) => ownerState.onContextMenu(event, node, ownerState.setContextMenuData, ownerState.setContextMenuOpen)}
              searchTerm={ownerState.searchTerm}
            />
          ))}
        </List>
      }
      <FsDirentMenu
        node={ownerState.contextMenuData?.node || undefined}
        anchorPosition={ownerState.contextMenuData?.anchorPosition || undefined}
        open={ownerState.isContextMenuOpen}
        onClose={ownerState.onContextMenuClose}
        onExited={() => ownerState.setContextMenuData(undefined)}
      />
    </FsExplorerRoot>
  );
}