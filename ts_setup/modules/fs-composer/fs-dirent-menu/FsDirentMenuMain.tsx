import React from 'react';
import { MenuItem, Divider, Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { useUtilityClasses } from './useUtilityClasses';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentMenuDeleteDialog } from './FsDirentMenuDeleteDialog';
import { DateTime } from 'luxon';


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

  const lastUpdated = dirent?.commitIndex?.updatedAt;
  const formatted = lastUpdated ? DateTime.fromISO(lastUpdated).toFormat('d.M.yyyy HH:mm') : undefined;
  const lastUpdatedBy = dirent?.commitIndex?.updatedByAuthor;

  const isNotCopyableOrRenamable = dirent?.type === 'ARTICLE_LINK'
    || dirent?.type === 'DIALOB_FORM_META'
    || dirent?.type === 'FOLDER'
    || dirent?.type === 'LOCALE'
    || dirent?.type === 'ARTICLE_PAGE'
    || dirent?.type === 'PRINTOUT_PAGE'
    || dirent?.type === 'UNKNOWN'

  function handleEdit() {
    if (dirent) {
      openAsset(dirent);
    }
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
        {dirent?.type !== 'FOLDER' && <Typography variant='caption'>
          {intl.formatMessage({ id: 'fs.direntMenu.header.lastEdited' },
            { updated: formatted, user: lastUpdatedBy })}
        </Typography>}
      </Box>

      <Divider className={classes.divider} />

      <MenuItem disableRipple className={props.openSubmenu === 'new' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('new')}
      >
        <FsIcon icon={FsIcons.New} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.new' })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem disableRipple className={classes.menuItem} onClick={handleEdit}>
        <FsIcon icon={FsIcons.Edit} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.edit' })}
      </MenuItem>

      <MenuItem disableRipple disabled className={dirent?.props?.locked ? classes.menuItemLocked : classes.menuItemUnlocked} onClick={handleLock}>
        {dirent?.props?.locked ? (<FsIcon icon={FsIcons.Locked} small />) : (<FsIcon icon={FsIcons.Unlocked} small />)}
        {dirent?.props?.locked ? intl.formatMessage({ id: 'fs.direntMenu.menuItem.unlock' }) : intl.formatMessage({ id: 'fs.direntMenu.menuItem.lock' })}
      </MenuItem>

      <MenuItem disableRipple disabled={isNotCopyableOrRenamable}
        className={props.openSubmenu === 'copy' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('copy')}
      >
        <FsIcon icon={FsIcons.Copy} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.duplicate' })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem disableRipple disabled={isNotCopyableOrRenamable}
        className={props.openSubmenu === 'rename' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('rename')}
      >
        <FsIcon icon={FsIcons.Rename} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.rename' })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <MenuItem disableRipple
        className={props.openSubmenu === 'description' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('description')}
      >
        <FsIcon icon={FsIcons.Description} small />
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

      <Divider className={classes.divider} />

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


