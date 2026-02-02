import React from 'react';
import { MenuItem, Divider, Typography, TextField, Box, Collapse, Chip } from '@mui/material';
import {
  DeleteForever as DeleteIcon,
  Add as NewIcon,
  Edit as EditIcon,
  ContentCopy as CopyIcon,
  DriveFileRenameOutline as RenameIcon,
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon,
  Construction as DevModeIcon,
  Assignment as AssignmentIcon,
  Block as DisabledIcon,
  VisibilityOff as AnonymousIcon,
} from '@mui/icons-material';
import { TreeNode } from '../../eveli-tree-api';
import { useUtilityClasses, EveliTreeItemMenuRoot, MENU_WIDTH } from './useUtilityClasses';
import { EveliTreeItemSharingPermissions } from './EveliTreeItemSharingPermissions';


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
  const [labelsExpanded, setLabelsExpanded] = React.useState(false);
  const [commentsExpanded, setCommentsExpanded] = React.useState(false);
  const [sharingExpanded, setSharingExpanded] = React.useState(false);

  // Reset all expander states when menu closes
  React.useEffect(() => {
    if (!props.open) {
      setLabelsExpanded(false);
      setCommentsExpanded(false);
      setSharingExpanded(false);
    }
  }, [props.open]);

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

  return (
    <EveliTreeItemMenuRoot className={classes.root} open={props.open} onClose={props.onClose}
      anchorReference="anchorPosition"
      anchorPosition={props.anchorPosition || undefined}
      transformOrigin={{
        vertical: 'top',
        horizontal: 'left',
      }}
      slotProps={{
        transition: {
          onExited: props.onExited,
        },
      }}
    >
      <Box className={classes.nodeNameContainer}>
        <Typography variant='subtitle2'>
          {props.node?.name}
        </Typography>
        <Typography variant='caption'>
          Last edited: 12.05.2025 by John Smith
        </Typography>
      </Box>

      <Divider className={classes.divider} />

      <MenuItem className={classes.menuItem} onClick={handleNew}>
        <NewIcon fontSize='small' />
        New
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleEdit}>
        <EditIcon fontSize='small' />
        Edit
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleCopy}>
        <CopyIcon fontSize='small' />
        Copy
      </MenuItem>
      <MenuItem className={classes.menuItem} onClick={handleDuplicate}>
        <RenameIcon fontSize='small' />
        Rename
      </MenuItem>

      <Divider className={classes.divider} />

      {props.node?.configOptions && props.node.configOptions.length > 0 && (
        <>
          <MenuItem className={classes.menuItem}>
            <ExpandMoreIcon fontSize='small' sx={{ visibility: 'hidden' }} />
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
                      {config.devMode && (
                        <Chip icon={<DevModeIcon />} label="Development" size='small' className={classes.label} />
                      )}
                      {config.assignableMode && (
                        <Chip icon={<AssignmentIcon />} label="Assignable" size='small' className={classes.label} />
                      )}
                      {config.disabledMode && (
                        <Chip icon={<DisabledIcon />} label="Disabled" size='small' className={classes.label} />
                      )}
                      {config.anonymousMode && (
                        <Chip icon={<AnonymousIcon />} label="Anonymous" size='small' className={classes.label} />
                      )}
                    </React.Fragment>
                  ))}
                </Box>
              </div>
            </div>
          </MenuItem>
          <Divider className={classes.divider} />
        </>
      )}

      <Divider className={classes.divider} />


      <MenuItem className={classes.menuItem} onClick={() => setLabelsExpanded(!labelsExpanded)}>
        {labelsExpanded ? <ExpandMoreIcon fontSize='small' /> : <ChevronRightIcon fontSize='small' />}
        <div>
          <div>Labels</div>
          <div>{props.node?.labels && props.node.labels.length > 0 && (
            <Box sx={{
              mt: 0.5,
              display: 'flex',
              flexWrap: 'wrap',
              gap: 0.5,
              maxWidth: MENU_WIDTH - 32, // Account for menu padding
              overflow: 'hidden'
            }}>
              {props.node.labels.map(label => (
                <Chip key={label.id} label={label.value} size='small' className={classes.label} />
              ))}
            </Box>
          )}
          </div>
        </div>
      </MenuItem>

      <Collapse in={labelsExpanded}>
        <Box className={classes.expandedContent}>
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
        </Box>
      </Collapse>

      <Divider className={classes.divider} />

      <MenuItem className={classes.menuItem} onClick={() => setCommentsExpanded(!commentsExpanded)}>
        {commentsExpanded ? <ExpandMoreIcon fontSize='small' /> : <ChevronRightIcon fontSize='small' />}
        Comments
      </MenuItem>
      <Collapse in={commentsExpanded}>
        <Box className={classes.expandedContent}>
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
        </Box>
      </Collapse>

      <Divider className={classes.divider} />

      <MenuItem className={classes.menuItem} onClick={() => setSharingExpanded(!sharingExpanded)}>
        {sharingExpanded ? <ExpandMoreIcon fontSize='small' /> : <ChevronRightIcon fontSize='small' />}
        Sharing and Permissions
      </MenuItem>
      <Collapse in={sharingExpanded}>
        <Box className={classes.expandedContent}>
          <EveliTreeItemSharingPermissions />
        </Box>
      </Collapse>

      <Divider className={classes.divider} />

      <MenuItem className={classes.menuItemDelete} onClick={handleDelete}>
        <DeleteIcon fontSize='small' />
        Delete
      </MenuItem>
    </EveliTreeItemMenuRoot>
  );
};

