import React from 'react';
import { MenuItem, Divider, Typography, Box, Chip } from '@mui/material';
import {
  DeleteForever as DeleteIcon,
  Add as NewIcon,
  Edit as EditIcon,
  ContentCopy as CopyIcon,
  DriveFileRenameOutline as RenameIcon,
  ChevronRight as ChevronRightIcon,
  Construction as DevModeIcon,
  Assignment as AssignmentIcon,
  Block as DisabledIcon,
  VisibilityOff as AnonymousIcon,
  Lock as LockedIcon,
  LockOpen as UnlockedIcon,
} from '@mui/icons-material';
import { TreeNode, mockTreeData } from '../../eveli-tree-api';
import { useUtilityClasses, MENU_WIDTH } from './useUtilityClasses';

function getReferencesCount(nodeId: string, nodeName: string): number {
  let count = 0;

  function searchInNode(node: TreeNode): void {
    // Check if this node is a reference to our target node
    if (node.isReference && node.name === nodeName && node.id !== nodeId) {
      count++;
    }

    // Recursively search children
    if (node.children) {
      node.children.forEach(child => searchInNode(child));
    }
  }

  // Search through all mock data
  mockTreeData.forEach(rootNode => searchInNode(rootNode));

  return count;
}

export interface EveliTreeItemMenuMainProps {
  node: TreeNode | undefined;
  openSubmenu: string | undefined;
  onSubmenuOpen: (submenuType: string) => void;
  onClose: () => void;
}

export const EveliTreeItemMenuMain: React.FC<EveliTreeItemMenuMainProps> = (props) => {
  const classes = useUtilityClasses();

  // Calculate reference count for this node
  const referencesCount = props.node ? getReferencesCount(props.node.id, props.node.name) : 0;

  function handleEdit() {
    console.log('Edit:', props.node?.name);
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
    const action = props.node?.isLocked ? 'Unlock' : 'Lock';
    console.log(`${action}:`, props.node?.name);
    props.onClose();
  }

  function handleSubmenuToggle(submenuType: string) {
    if (props.openSubmenu === submenuType) {
      props.onSubmenuOpen(''); // Close the submenu by passing empty string
    } else {
      props.onSubmenuOpen(submenuType); // Open the submenu
    }
  }

  return (
    <Box className={classes.sectionMain}>
      <Box className={classes.headerMain}>
        <Typography variant='h3' paddingBottom={0}>{props.node?.name}</Typography>
        <Typography variant='caption'>
          Last edited: 12.05.2025 by John Smith
        </Typography>
      </Box>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'new' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('new')}
      >
        <NewIcon fontSize='small' />New
        <Box flex={1} />
        <ChevronRightIcon fontSize='small' />
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleEdit}>
        <EditIcon fontSize='small' />Edit</MenuItem>
      <MenuItem
        className={props.node?.isLocked ? classes.menuItemLocked : classes.menuItemUnlocked}
        onClick={handleLock}
      >
        {props.node?.isLocked ? (
          <LockedIcon fontSize='small' />
        ) : (
          <UnlockedIcon fontSize='small' />
        )}
        {props.node?.isLocked ? 'Unlock' : 'Lock'}
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleCopy}>
        <CopyIcon fontSize='small' />Copy</MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleDuplicate}>
        <RenameIcon fontSize='small' />Rename</MenuItem>
      <MenuItem className={classes.menuItemDelete} onClick={handleDelete}>
        <DeleteIcon fontSize='small' />Delete</MenuItem>

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
                    {config.devMode && (<Chip icon={<DevModeIcon />} label="Development" size='small' className={classes.label} />)}
                    {config.assignableMode && (<Chip icon={<AssignmentIcon />} label="Assignable" size='small' className={classes.label} />)}
                    {config.disabledMode && (<Chip icon={<DisabledIcon />} label="Disabled" size='small' className={classes.label} />)}
                    {config.anonymousMode && (<Chip icon={<AnonymousIcon />} label="Anonymous" size='small' className={classes.label} />)}
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
        <ChevronRightIcon fontSize='small' />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'comments' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('comments')}
      >
        Comments ({props.node?.comments?.length || 0})
        <Box flex={1} />
        <ChevronRightIcon fontSize='small' />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'sharing' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('sharing')}
      >
        Sharing and Permissions
        <Box flex={1} />
        <ChevronRightIcon fontSize='small' />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'history' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('history')}
      >
        History
        <Box flex={1} />
        <ChevronRightIcon fontSize='small' />
      </MenuItem>

      <Divider className={classes.divider} />

      <MenuItem
        className={props.openSubmenu === 'references' ? classes.menuItemActive : classes.menuItem}
        onClick={() => handleSubmenuToggle('references')}
      >
        References ({referencesCount})
        <Box flex={1} />
        <ChevronRightIcon fontSize='small' />
      </MenuItem>
    </Box>
  );
};