import React from 'react';
import { MenuItem, Divider, Typography, Box, Chip } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsNav, useFsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses, MENU_WIDTH } from './useUtilityClasses';
import { FsIcon, FsIcons } from '../fs-theme';


export interface FsDirentMenuMainProps {
  dirent: Fs.DirentBase | undefined;
  openSubmenu: string | undefined;
  onSubmenuOpen: (submenuType: string) => void;
  onClose: () => void;
}

export const FsDirentMenuMain: React.FC<FsDirentMenuMainProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { openAsset } = useFsNav();
  const { findReferencesToDirent } = useFsDirent();
  const dirent = props.dirent;

  const referencesCount = dirent ? findReferencesToDirent(dirent).length : 0;
  const changes = dirent?.changes ?? [];
  const lastChange = changes.length > 0 ? changes[changes.length - 1] : undefined;

  function handleEdit() {
    if (dirent) {
      openAsset(dirent, dirent.name);
    }
    props.onClose();
  }

  function handleCopy() {
    console.log('Copy:', dirent?.name);
    props.onClose();
  }

  function handleDelete() {
    console.log('Delete:', dirent?.name);
    props.onClose();
  }

  function handleDuplicate() {
    console.log('Duplicate:', dirent?.name);
    props.onClose();
  }

  function handleLock() {
    const action = dirent?.locked ? 'Unlock' : 'Lock';
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

  return (
    <Box className={classes.sectionMain}>
      <Box className={classes.headerMain}>
        <Typography variant='h3' paddingBottom={0} paddingTop={0}>{dirent?.name}</Typography>
        <Typography variant='caption'>
          {intl.formatMessage({ id: 'fs.direntMenu.header.lastEdited' }, { updated: lastChange?.changeDate, user: lastChange?.changedBy.userName })}
        </Typography>
      </Box>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'new' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('new')}
      >
        <FsIcon icon={FsIcons.New} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.new' })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <MenuItem className={classes.menuItem} onClick={handleEdit}>
        <FsIcon icon={FsIcons.Edit} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.edit' })}
      </MenuItem>

      <MenuItem
        className={dirent?.locked ? classes.menuItemLocked : classes.menuItemUnlocked}
        onClick={handleLock}
      >
        {dirent?.locked ? (<FsIcon icon={FsIcons.Locked} small />) : (<FsIcon icon={FsIcons.Unlocked} small />)}
        {dirent?.locked ? intl.formatMessage({ id: 'fs.direntMenu.menuItem.unlock' }) : intl.formatMessage({ id: 'fs.direntMenu.menuItem.lock' })}
      </MenuItem>

      <MenuItem className={classes.menuItem} onClick={handleCopy}>
        <FsIcon icon={FsIcons.Copy} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.copy' })}
      </MenuItem>

      <MenuItem
        className={props.openSubmenu === 'rename' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('rename')}
      >
        <FsIcon icon={FsIcons.Rename} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.rename' })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <MenuItem className={classes.menuItemDelete} onClick={handleDelete}>
        <FsIcon icon={FsIcons.Delete} small />
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.delete' })}
      </MenuItem>

      <Divider className={classes.divider} />

      {dirent && dirent.configOptions.length > 0 && (
        <MenuItem className={classes.menuItem}>
          <div>
            <div>{intl.formatMessage({ id: 'fs.direntMenu.menuItem.configOptions' })}</div>
            <div>
              <Box sx={{ mt: 0.5, display: 'flex', flexWrap: 'wrap', gap: 0.5, maxWidth: MENU_WIDTH - 32, overflow: 'hidden' }}>
                {dirent.configOptions.includes('devMode') && (<Chip icon={<FsIcon icon={FsIcons.DevMode} />} label={intl.formatMessage({ id: 'fs.direntMenu.chip.devMode' })} size='small' className={classes.label} />)}
                {dirent.configOptions.includes('assignableMode') && (<Chip icon={<FsIcon icon={FsIcons.Assignment} />} label={intl.formatMessage({ id: 'fs.direntMenu.chip.assignable' })} size='small' className={classes.label} />)}
                {dirent.configOptions.includes('disabledMode') && (<Chip icon={<FsIcon icon={FsIcons.Disabled} />} label={intl.formatMessage({ id: 'fs.direntMenu.chip.disabled' })} size='small' className={classes.label} />)}
                {dirent.configOptions.includes('anonymousMode') && (<Chip icon={<FsIcon icon={FsIcons.Anonymous} />} label={intl.formatMessage({ id: 'fs.direntMenu.chip.anonymous' })} size='small' className={classes.label} />)}
              </Box>
            </div>
          </div>
        </MenuItem>
      )}

      <Divider className={classes.divider} />

      <MenuItem className={props.openSubmenu === 'labels' ? classes.menuItemActive : classes.menuItem} onClick={() => handleSubmenuToggle('labels')}>
        <div>
          <div>{intl.formatMessage({ id: 'fs.direntMenu.menuItem.labels' })}</div>
          {dirent && dirent.labels.length > 0 && (
            <Box sx={{
              mt: 0.5,
              display: 'flex',
              flexWrap: 'wrap',
              gap: 0.5,
              overflow: 'hidden'
            }}>
              {dirent.labels.map(label => (
                <Chip key={label.id} label={label.value} size='small' className={classes.label} />
              ))}
            </Box>
          )}
        </div>
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'comments' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('comments')}
      >
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.comments' }, { count: dirent?.comments.length ?? 0 })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'sharing' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('sharing')}
      >
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.sharing' })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'history' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('history')}
      >
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.history' })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'references' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('references')}
      >
        {intl.formatMessage({ id: 'fs.direntMenu.menuItem.references' }, { count: referencesCount })}
        <Box flex={1} />
        <FsIcon icon={FsIcons.ChevronRight} small />
      </MenuItem>
    </Box>
  );
};
