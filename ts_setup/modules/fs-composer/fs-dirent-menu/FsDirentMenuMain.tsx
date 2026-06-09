import React from 'react';
import { MenuItem, Divider, Typography, Box, Chip } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { useUtilityClasses, MENU_WIDTH } from './useUtilityClasses';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentMenuDeleteDialog } from './FsDirentMenuDeleteDialog';


export interface FsDirentMenuMainProps {
  dirent: Fs.DirentBase | undefined;
  openSubmenu: string | undefined;
  onSubmenuOpen: (submenuType: string) => void;
  onClose: () => void;
}

export const FsDirentMenuMain: React.FC<FsDirentMenuMainProps> = React.memo((props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { openAsset } = useFsNav();
  const { deleteDirent } = useFsDirent();
  const [deleteDialogOpen, setDeleteDialogOpen] = React.useState(false);
  const dirent = props.dirent;

  const changes = dirent?.props?.changes ?? [];
  const lastChange = changes.length > 0 ? changes[changes.length - 1] : undefined;

  function handleEdit() {
    if (dirent) {
      openAsset(dirent);
    }
    props.onClose();
  }

  function handleCopy() {
    console.log('Copy:', dirent?.name);
    props.onClose();
  }

  function handleDelete() {
    setDeleteDialogOpen(true);
  }

  async function handleConfirmDelete() {
    setDeleteDialogOpen(false);
    props.onClose();
    if (dirent) {
      await deleteDirent(dirent.id, dirent.type);
    }
  }

  function handleDuplicate() {
    console.log('Duplicate:', dirent?.name);
    props.onClose();
  }

  function handleLock() {
    const action = dirent?.props?.locked ? 'Unlock' : 'Lock';
    console.log(`${action}:`, dirent?.name);
    props.onClose();
  }

  function handleSubmenuToggle(submenuType: string) {
    if (props.openSubmenu === submenuType) {
      props.onSubmenuOpen('');
    } else {
      props.onSubmenuOpen(submenuType);
    }
  }

  return (<>
    <Box className={classes.sectionMain}>
      <Box className={classes.headerMain}>
        <Typography variant='h3' paddingBottom={0} paddingTop={0}>{dirent?.name}</Typography>
        <Typography variant='caption'>
          {intl.formatMessage({ id: 'fs.direntMenu.header.lastEdited' }, { updated: lastChange?.changeDate, user: lastChange?.changedBy.userName })}
        </Typography>
      </Box>

      <Divider className={classes.divider} />

      <MenuItem disableRipple
        className={props.openSubmenu === 'new' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('new')}
      >
        <FsIcon icon={FsIcons.New} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.new' })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <MenuItem disableRipple className={classes.menuItem} onClick={handleEdit}>
        <FsIcon icon={FsIcons.Edit} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.edit' })}
      </MenuItem>

      <MenuItem disableRipple
        className={dirent?.props?.locked ? classes.menuItemLocked : classes.menuItemUnlocked}
        onClick={handleLock}
      >
        {dirent?.props?.locked ? (<FsIcon icon={FsIcons.Locked} small />) : (<FsIcon icon={FsIcons.Unlocked} small />)}
        {dirent?.props?.locked ? intl.formatMessage({ id: 'fs.direntMenu.menuItem.unlock' }) : intl.formatMessage({ id: 'fs.direntMenu.menuItem.lock' })}
      </MenuItem>

      <MenuItem disableRipple className={classes.menuItem} onClick={handleCopy}>
        <FsIcon icon={FsIcons.Copy} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.copy' })}
      </MenuItem>

      <MenuItem disableRipple
        className={props.openSubmenu === 'rename' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('rename')}
      >
        <FsIcon icon={FsIcons.Rename} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.rename' })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <MenuItem disableRipple className={classes.menuItem} onClick={handleDuplicate}>
        <FsIcon icon={FsIcons.Copy} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.duplicate' })}
      </MenuItem>

      <MenuItem disableRipple
        className={props.openSubmenu === 'description' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('description')}
      >
        <FsIcon icon={FsIcons.Form} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.description' })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <MenuItem disableRipple
        className={props.openSubmenu === 'labels' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('labels')}
      >
        <FsIcon icon={FsIcons.Label} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.labels' })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <MenuItem disableRipple className={classes.menuItemDelete} onClick={handleDelete}>
        <FsIcon icon={FsIcons.Delete} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.delete' })}
      </MenuItem>
    </Box>

    {deleteDialogOpen && dirent && (
      <FsDirentMenuDeleteDialog
        dirent={dirent}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={handleConfirmDelete}
      />
    )}

  </>
  )
})


