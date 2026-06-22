import React from 'react';
import { Box, Typography, List, IconButton, Badge, Popover } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcons, FsIcon } from '../fs-theme';
import { Fs } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { FsDirent } from '../fs-dirent';
import { FsDirentMenu } from '../fs-dirent-menu';
import { FsSearch } from '../fs-search';
import { FsExplorerNoSearchResults } from './FsExplorerNoSearchResults';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsExplorerRoot } from './useUtilityClasses';
import { allWidgets } from '../fs-factory';

export const FsExplorer: React.FC = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const ownerState = useOwnerState();
  const { openCreateTab, isExpanded } = useFsNav();
  const [newFileAnchorEl, setNewFileAnchorEl] = React.useState<HTMLElement | null>(null);

  return (
    <FsExplorerRoot className={classes.root}>
      <Box sx={{ position: 'sticky', top: 0, zIndex: 1 }}>
        <Box className={classes.title}>
          <Typography className={classes.titleText} mr={3}>{intl.formatMessage({ id: 'fs.explorer.title' })}</Typography>
          <Box flexGrow={1} />

          <IconButton className={classes.iconLight} onClick={() => ownerState.setSearchExpanded(!ownerState.isSearchExpanded)}>
            <Badge className={classes.badgeLight}
              badgeContent={ownerState.isSearchExpanded ? <FsIcon icon={FsIcons.Close} xsmall /> : undefined}
              anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            >
              <FsIcon icon={FsIcons.Search} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.toggleSearch' })} />
            </Badge>
          </IconButton>


          <IconButton className={classes.iconLight} onClick={(e) => setNewFileAnchorEl(e.currentTarget)}>
            <Badge className={classes.badgeLight} badgeContent={<FsIcon icon={FsIcons.Add} xsmall />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FsIcon icon={FsIcons.File} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.newFile' })} />
            </Badge>
          </IconButton>

          <Popover
            open={!!newFileAnchorEl}
            anchorEl={newFileAnchorEl}
            onClose={() => setNewFileAnchorEl(null)}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
            transformOrigin={{ vertical: 'top', horizontal: 'left' }}
          >
            <NewDirent onClose={() => setNewFileAnchorEl(null)} />
          </Popover>

          <IconButton className={classes.iconLight} onClick={() => openCreateTab('FOLDER', undefined)}>
            <Badge className={classes.badgeLight} badgeContent={<FsIcon icon={FsIcons.Add} xsmall />} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
              <FsIcon icon={FsIcons.Folder} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.newFolder' })} />
            </Badge>
          </IconButton>

          <IconButton className={classes.iconLight}
            onClick={() => ownerState.collapseAll()}
            disabled={!ownerState.isAnyDirentExpanded}
          >
            <FsIcon icon={FsIcons.CollapseAll} small tooltip={intl.formatMessage({ id: 'fs.explorer.tooltip.collapseAll' })} />
          </IconButton>


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
              isExpanded={isExpanded}
              activeDirentId={ownerState.activeDirentId}
              openAsset={ownerState.openAsset}
              onToggle={ownerState.toggleDirent}
              onContextMenu={ownerState.onContextMenu}
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


const NewDirent: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const intl = useIntl();
  const { openCreateTab } = useFsNav();


  function handleTypeClick(type: Fs.BodyType) {
    openCreateTab(type, undefined);
    onClose();
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', p: 1, minWidth: 180 }}>
      {allWidgets.filter(w => w.meta.type !== 'FOLDER').map((widget) => {

        return (
          <Box key={widget.meta.type} onClick={() => handleTypeClick(widget.meta.type)}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              py: 0.75,
              cursor: 'pointer',
              borderRadius: 1,
              '&:hover': { bgcolor: 'action.hover' }
            }}
          >

            <widget.icons.dirent.Collapsed small />
            <Typography variant='subtitle2'>{intl.formatMessage({ id: `fs.direntNew.type.${widget.meta.type.toLocaleLowerCase()}` })}</Typography>
          </Box>
        )
      })}


    </Box>
  );
};