import React from 'react';
import { MenuItem, Divider, Typography, TextField, Box, Collapse, Chip } from '@mui/material';
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
} from '@mui/icons-material';
import { TreeNode } from '../../eveli-tree-api';
import { useUtilityClasses, EveliTreeItemMenuRoot, MENU_WIDTH, MENU_HEIGHT } from './useUtilityClasses';
import { EveliTreeItemSharingPermissions } from './EveliTreeItemSharingPermissions';
import { EveliTreeItemHistory } from './EveliTreeItemHistory';
import { NewItem } from './NewItem';


interface EveliTreeItemMenuProps {
  node: TreeNode | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

export const EveliTreeItemMenu: React.FC<EveliTreeItemMenuProps> = (props) => {
  const classes = useUtilityClasses();
  const [labels, setLabels] = React.useState('');
  const [comments, setComments] = React.useState('');
  const [openSubmenu, setOpenSubmenu] = React.useState<string | undefined>(undefined);

  // Calculate if menu should expand upward based on available space
  const shouldExpandUpward = React.useMemo(() => {
    if (!props.anchorPosition) {
      return false;
    }
    const viewportHeight = window.innerHeight;
    const clickY = props.anchorPosition.top;
    const spaceBelow = viewportHeight - clickY;

    return spaceBelow < MENU_HEIGHT && clickY > MENU_HEIGHT;
  }, [props.anchorPosition]);

  React.useEffect(() => {
    if (!props.open) {
      setOpenSubmenu(undefined);
    }
  }, [props.open]);


  function handleSubmenuOpen(event: React.MouseEvent<HTMLElement>, submenuType: string) {
    setOpenSubmenu(submenuType);
  }

  function handleSubmenuClose() {
    setOpenSubmenu(undefined);
  }

  function handleNew() {
    console.log('New:', props.node?.name);
    props.onClose();
  }

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

  return (<>
    <EveliTreeItemMenuRoot
      className={classes.root}
      open={props.open}
      onClose={props.onClose}
      isSubmenuOpen={!!openSubmenu}
      anchorReference="anchorPosition"
      anchorPosition={props.anchorPosition || undefined}
      anchorOrigin={{
        vertical: shouldExpandUpward ? 'bottom' : 'top',
        horizontal: 'left',
      }}
      transformOrigin={{
        vertical: shouldExpandUpward ? 'bottom' : 'top',
        horizontal: 'left',
      }}
      slotProps={{
        transition: {
          onExited: props.onExited,
        },
      }}
    >
      <Box className={classes.menuContainer}>
        {/* Left section - main menu */}
        <Box className={classes.leftMenuSection}>
          <Box className={classes.nodeNameContainer}>
            <Typography variant='subtitle2'>
              {props.node?.name}
            </Typography>
            <Typography variant='caption'>
              Last edited: 12.05.2025 by John Smith
            </Typography>
          </Box>

          <Divider className={classes.divider} />

          <MenuItem
            className={openSubmenu === 'new' ? classes.menuItemActive : classes.menuItem}
            onClick={(e) => handleSubmenuOpen(e, 'new')}
          >
            <NewIcon fontSize='small' />New
            <Box flex={1} />
            <ChevronRightIcon fontSize='small' />
          </MenuItem>
          <MenuItem className={classes.menuItem} onClick={handleEdit}>
            <EditIcon fontSize='small' />Edit</MenuItem>
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
            className={openSubmenu === 'labels' ? classes.menuItemActive : classes.menuItem}
            onClick={(e) => handleSubmenuOpen(e, 'labels')}
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
            className={openSubmenu === 'comments' ? classes.menuItemActive : classes.menuItem}
            onClick={(e) => handleSubmenuOpen(e, 'comments')}
          >
            Comments
            <Box flex={1} />
            <ChevronRightIcon fontSize='small' />
          </MenuItem>

          <Divider className={classes.divider} />

          <MenuItem
            className={openSubmenu === 'sharing' ? classes.menuItemActive : classes.menuItem}
            onClick={(e) => handleSubmenuOpen(e, 'sharing')}
          >
            Sharing and Permissions
            <Box flex={1} />
            <ChevronRightIcon fontSize='small' />
          </MenuItem>

          <Divider className={classes.divider} />

          <MenuItem
            className={openSubmenu === 'history' ? classes.menuItemActive : classes.menuItem}
            onClick={(e) => handleSubmenuOpen(e, 'history')}
          >
            History
            <Box flex={1} />
            <ChevronRightIcon fontSize='small' />
          </MenuItem>
        </Box>

        {/* Conditional divider and right section */}
        <Collapse orientation="horizontal" in={!!openSubmenu}>
          <Divider orientation="vertical" className={classes.menuDivider} />
          <Box className={classes.submenuSection}>
            {openSubmenu === 'labels' && (
              <TextField
                className={classes.textField}
                multiline
                minRows={2}
                maxRows={5}
                value={labels}
                onChange={(e) => setLabels(e.target.value)}
                placeholder='Add labels...'
                size='small'
              />
            )}
            {openSubmenu === 'comments' && (
              <TextField
                className={classes.textField}
                multiline
                minRows={2}
                maxRows={5}
                value={comments}
                onChange={(e) => setComments(e.target.value)}
                placeholder='Add comments...'
                size='small'
              />
            )}
            {openSubmenu === 'sharing' && <EveliTreeItemSharingPermissions />}
            {openSubmenu === 'history' && <EveliTreeItemHistory />}
            {openSubmenu === 'new' && <NewItem />}
          </Box>
        </Collapse>
      </Box>
    </EveliTreeItemMenuRoot>
  </>
  );
};

