import React from 'react';
import { MenuItem, Divider, Typography, Box, Chip } from '@mui/material';
import { FsNode, mockFsData, useFs } from '@dxs-ts/fs-api';
import { useUtilityClasses, MENU_WIDTH } from './useUtilityClasses';
import { FsIcons } from '../fs-theme';

function getReferencesCount(nodeId: string, nodeName: string): number {
  let count = 0;
  function searchInNode(node: FsNode): void {
    if (node.reference && node.name === nodeName && node.id !== nodeId) {
      count++;
    }

    if (node.children) {
      node.children.forEach(child => searchInNode(child));
    }
  }

  mockFsData.forEach(rootNode => searchInNode(rootNode));

  return count;
}

export interface FsNodeMenuMainProps {
  node: FsNode | undefined;
  openSubmenu: string | undefined;
  onSubmenuOpen: (submenuType: string) => void;
  onClose: () => void;
}

export const FsNodeMenuMain: React.FC<FsNodeMenuMainProps> = (props) => {
  const classes = useUtilityClasses();
  const { openAsset } = useFs();

  const referencesCount = props.node ? getReferencesCount(props.node.id, props.node.name) : 0;

  function handleEdit() {
    if (props.node) {
      openAsset(props.node, props.node.name);
    }
    props.onClose();
  }

  function handleCopy() {
    console.log('Copy:', props.node?.name);
    props.onClose();
  }

  function handleDelete() {
    console.log('Delete:', props.node?.name);
    props.onClose();
  }

  function handleDuplicate() {
    console.log('Duplicate:', props.node?.name);
    props.onClose();
  }

  function handleLock() {
    const action = props.node?.locked ? 'Unlock' : 'Lock';
    console.log(`${action}:`, props.node?.name);
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
        <Typography variant='h3' paddingBottom={0} paddingTop={0}>{props.node?.name}</Typography>
        <Typography variant='caption'>
          Last edited: 12.05.2025 by John Smith
        </Typography>
      </Box>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'new' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('new')}
      >
        <FsIcons.New fontSize='small' />New
        <Box flex={1} />
        <FsIcons.ChevronRight fontSize='small' />
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleEdit}>
        <FsIcons.Edit fontSize='small' />Edit</MenuItem>
      <MenuItem
        className={props.node?.locked ? classes.menuItemLocked : classes.menuItemUnlocked}
        onClick={handleLock}
      >
        {props.node?.locked ? (
          <FsIcons.Locked fontSize='small' />
        ) : (
          <FsIcons.Unlocked fontSize='small' />
        )}
        {props.node?.locked ? 'Unlock' : 'Lock'}
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleCopy}>
        <FsIcons.Copy fontSize='small' />Copy</MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleDuplicate}>
        <FsIcons.Rename fontSize='small' />Rename</MenuItem>
      <MenuItem className={classes.menuItemDelete} onClick={handleDelete}>
        <FsIcons.Delete fontSize='small' />Delete</MenuItem>

      <Divider className={classes.divider} />

      {props.node?.configOptions && props.node.configOptions.length > 0 && (
        <MenuItem className={classes.menuItem}>
          <div>
            <div>Configuration Options</div>
            <div>
              <Box sx={{
                mt: 0.5,
                display: 'flex',
                flexWrap: 'wrap',
                gap: 0.5,
                maxWidth: MENU_WIDTH - 32,
                overflow: 'hidden'
              }}>
                {props.node.configOptions.map((config, index) => (
                  <React.Fragment key={index}>
                    {config.devMode && (<Chip icon={<FsIcons.DevMode />} label="Development" size='small' className={classes.label} />)}
                    {config.assignableMode && (<Chip icon={<FsIcons.Assignment />} label="Assignable" size='small' className={classes.label} />)}
                    {config.disabledMode && (<Chip icon={<FsIcons.Disabled />} label="Disabled" size='small' className={classes.label} />)}
                    {config.anonymousMode && (<Chip icon={<FsIcons.Anonymous />} label="Anonymous" size='small' className={classes.label} />)}
                  </React.Fragment>
                ))}
              </Box>
            </div>
          </div>
        </MenuItem>
      )}

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'labels' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('labels')}
      >
        <div>
          <div>Labels</div>
          {props.node?.labels && props.node.labels.length > 0 && (
            <Box sx={{
              mt: 0.5,
              display: 'flex',
              flexWrap: 'wrap',
              gap: 0.5,
              overflow: 'hidden'
            }}>
              {props.node.labels.map(label => (
                <Chip key={label.id} label={label.value} size='small' className={classes.label} />
              ))}
            </Box>
          )}
        </div>
        <Box flex={1} />
        <FsIcons.ChevronRight fontSize='small' />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'comments' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('comments')}
      >
        Comments ({props.node?.comments?.length || 0})
        <Box flex={1} />
        <FsIcons.ChevronRight fontSize='small' />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'sharing' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('sharing')}
      >
        Sharing and Permissions
        <Box flex={1} />
        <FsIcons.ChevronRight fontSize='small' />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'history' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('history')}
      >
        History
        <Box flex={1} />
        <FsIcons.ChevronRight fontSize='small' />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'references' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('references')}
      >
        References ({referencesCount})
        <Box flex={1} />
        <FsIcons.ChevronRight fontSize='small' />
      </MenuItem>
    </Box>
  );
};