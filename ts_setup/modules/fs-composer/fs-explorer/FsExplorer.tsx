import React from 'react';
import { Box, Typography, List, IconButton, Badge } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcons, FsIcon } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirent } from '../fs-dirent';
import { FsDirentMenu } from '../fs-dirent-menu';
import { FsSearch } from '../fs-search';
import { FsExplorerNoSearchResults } from './FsExplorerNoSearchResults';
import { FsExplorerProps } from './FsExplorerProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsExplorerRoot } from './useUtilityClasses';

export const FsExplorer: React.FC<FsExplorerProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const ownerState = useOwnerState(props);
  const { openCreateTab } = useFsNav();

  return (
    <FsExplorerRoot className={classes.root} isDarkTheme={ownerState.isDarkMode}>
      <Box sx={{ position: 'sticky', top: 0, zIndex: 1 }}>
      <Box className={classes.title}>
        <Typography className={classes.titleText} mr={3}>{intl.formatMessage({ id: 'fs.explorer.title' })}</Typography>
        <Box flexGrow={1} />
        {ownerState.isDarkMode ? (
          <IconButton className={classes.iconDark} onClick={() => ownerState.setSearchExpanded(!ownerState.isSearchExpanded)}>
            <Badge className={classes.badgeDark}
              badgeContent={ownerState.isSearchExpanded ? <FsIcon icon={FsIcons.Close} xsmall /> : undefined}
              anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            >
              <FsIcon icon={FsIcons.Search} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.toggleSearch' })} />
            </Badge>
          </IconButton>
        ) : (
          <IconButton className={classes.iconLight} onClick={() => ownerState.setSearchExpanded(!ownerState.isSearchExpanded)}>
              <Badge className={classes.badgeLight}
                badgeContent={ownerState.isSearchExpanded ? <FsIcon icon={FsIcons.Close} xsmall /> : undefined}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              >
              <FsIcon icon={FsIcons.Search} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.toggleSearch' })} />
            </Badge>
          </IconButton>
        )}
        {ownerState.isDarkMode ? (
          <IconButton className={classes.iconDark}>
            <Badge className={classes.badgeDark} badgeContent={<FsIcon icon={FsIcons.Add} xsmall />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FsIcon icon={FsIcons.File} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.newFile' })} />
            </Badge>
          </IconButton>
        ) : (
          <IconButton className={classes.iconLight}>
            <Badge className={classes.badgeLight} badgeContent={<FsIcon icon={FsIcons.Add} xsmall />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FsIcon icon={FsIcons.File} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.newFile' })} />
            </Badge>
          </IconButton>
        )}
        {ownerState.isDarkMode ? (
          <IconButton className={classes.iconDark} onClick={() => openCreateTab('FOLDER', undefined)}>
            <Badge className={classes.badgeDark} badgeContent={<FsIcon icon={FsIcons.Add} xsmall />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FsIcon icon={FsIcons.Folder} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.newFolder' })} />
            </Badge>
          </IconButton>
        ) : (
            <IconButton className={classes.iconLight} onClick={() => openCreateTab('FOLDER', undefined)}>
            <Badge className={classes.badgeLight} badgeContent={<FsIcon icon={FsIcons.Add} xsmall />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FsIcon icon={FsIcons.Folder} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.newFolder' })} />
            </Badge>
          </IconButton>
        )}

        {ownerState.isDarkMode ? (
          <IconButton className={classes.iconDark}
            onClick={() => ownerState.collapseAll()}
            disabled={!ownerState.isAnyDirentExpanded}
          >
            <FsIcon icon={FsIcons.CollapseAll} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.collapseAll' })} />
          </IconButton>
        ) : (
            <IconButton className={classes.iconLight}
              onClick={() => ownerState.collapseAll()}
              disabled={!ownerState.isAnyDirentExpanded}
            >
            <FsIcon icon={FsIcons.CollapseAll} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.collapseAll' })} />
          </IconButton>
        )}

        {ownerState.isDarkMode ? (
          <IconButton className={classes.iconDark} onClick={() => ownerState.setIsDarkMode(!ownerState.isDarkMode)}>
            <FsIcon icon={FsIcons.LightMode} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.toggleTheme' })} />
          </IconButton>
        ) : (
          <IconButton className={classes.iconLight} onClick={() => ownerState.setIsDarkMode(!ownerState.isDarkMode)}>
            <FsIcon icon={FsIcons.LightMode} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.toggleTheme' })} />
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
      </Box>

      {ownerState.filteredTreeData.length === 0 ? <FsExplorerNoSearchResults /> :
        <List component='nav' disablePadding>
          {ownerState.filteredTreeData.map((dirent) => (
            <FsDirent
              key={dirent.id}
              dirent={dirent}
              level={0}
              onToggle={(direntId) => ownerState.toggleDirent(direntId)}
              onContextMenu={(event, dirent) => ownerState.onContextMenu(event, dirent, ownerState.setContextMenuData, ownerState.setContextMenuOpen)}
              searchTerm={ownerState.searchTerm}
            />
          ))}
        </List>
      }
      <FsDirentMenu
        dirent={ownerState.contextMenuData?.dirent || undefined}
        anchorPosition={ownerState.contextMenuData?.anchorPosition || undefined}
        open={ownerState.isContextMenuOpen}
        onClose={ownerState.onContextMenuClose}
        onExited={() => ownerState.setContextMenuData(undefined)}
      />
    </FsExplorerRoot>
  );
}