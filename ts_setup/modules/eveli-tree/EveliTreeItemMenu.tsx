import React from 'react';
import { Menu, MenuItem, Divider, Typography, styled, useTheme, TextField, Box, Collapse } from '@mui/material';
import {
  DeleteForever as DeleteIcon,
  Add as NewIcon,
  Edit as EditIcon,
  ContentCopy as CopyIcon,
  DriveFileRenameOutline as RenameIcon,
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon
} from '@mui/icons-material';
import { TreeNode } from '../eveli-tree-api';

interface EveliTreeItemMenuProps {
  node: TreeNode | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

export const EveliTreeItemMenu: React.FC<EveliTreeItemMenuProps> = (props) => {
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
    <Menu open={props.open} onClose={props.onClose} anchorReference="anchorPosition" anchorPosition={props.anchorPosition || undefined}
      transformOrigin={{
        vertical: 'top',
        horizontal: 'left',
      }}
      slotProps={{
        transition: {
          onExited: props.onExited,
        },
        paper: {
          sx: {
            backgroundColor: '#2d2d30',
            color: '#cccccc',
            border: '1px solid #3c3c3c',
            minWidth: 300,
          }
        }
      }}

    >
      <StyledMenuItemNodeName node={props.node} />

      <Divider sx={{ borderColor: '#3c3c3c' }} />

      <StyledMenuItem onClick={handleNew} icon={<NewIcon fontSize="small" />}>New</StyledMenuItem>
      <StyledMenuItem onClick={handleEdit} icon={<EditIcon fontSize="small" />}>Edit</StyledMenuItem>
      <StyledMenuItem onClick={handleCopy} icon={<CopyIcon fontSize="small" />}>Copy</StyledMenuItem>
      <StyledMenuItem onClick={handleDuplicate} icon={<RenameIcon fontSize="small" />}>Rename</StyledMenuItem>

      <Divider sx={{ borderColor: '#3c3c3c' }} />

      <StyledMenuItem
        onClick={() => setLabelsExpanded(!labelsExpanded)}
        icon={labelsExpanded ? <ExpandMoreIcon fontSize="small" /> : <ChevronRightIcon fontSize="small" />}>
        Labels
      </StyledMenuItem>
      <Collapse in={labelsExpanded}>
        <Box sx={{ px: 2, pb: 1 }}>
          <StyledTextField
            multiline
            minRows={2}
            maxRows={5}
            value={labels}
            onChange={(e) => setLabels(e.target.value)}
            placeholder="Add labels..."
            size="small"
          />
        </Box>
      </Collapse>

      <StyledMenuItem
        onClick={() => setCommentsExpanded(!commentsExpanded)}
        icon={commentsExpanded ? <ExpandMoreIcon fontSize="small" /> : <ChevronRightIcon fontSize="small" />}>
        Comments
      </StyledMenuItem>
      <Collapse in={commentsExpanded}>
        <Box sx={{ px: 2, pb: 1 }}>
          <StyledTextField
            multiline
            minRows={2}
            maxRows={5}
            value={comments}
            onChange={(e) => setComments(e.target.value)}
            placeholder="Add comments..."
            size="small"
          />
        </Box>
      </Collapse>

      <StyledMenuItem
        onClick={() => setSharingExpanded(!sharingExpanded)}
        icon={sharingExpanded ? <ExpandMoreIcon fontSize="small" /> : <ChevronRightIcon fontSize="small" />}>
        Sharing and Permissions
      </StyledMenuItem>
      <Collapse in={sharingExpanded}>
        <Box sx={{ px: 2, pb: 1 }}>
          <Typography variant="body2" sx={{ color: '#888888', fontStyle: 'italic' }}>
            info here
          </Typography>
        </Box>
      </Collapse>

      <Divider sx={{ borderColor: '#3c3c3c' }} />

      <StyledDeleteMenuItem onClick={handleDelete} icon={<DeleteIcon fontSize="small" />}>Delete</StyledDeleteMenuItem>
    </Menu>
  );
};

interface StyledMenuItemProps {
  onClick?: () => void;
  icon?: React.ReactNode;
  children: React.ReactNode;
}

const StyledMenuItemBase = styled(MenuItem)(() => ({
  fontSize: '13px',
  display: 'flex',
  alignItems: 'center',
  gap: '8px',
  '&:hover': {
    backgroundColor: '#3c3c3c',
  },
  '& .MuiSvgIcon-root': {
    color: '#cccccc',
    fontSize: '16px',
  },
}));

const StyledMenuItem: React.FC<StyledMenuItemProps> = ({ onClick, icon, children }) => (
  <StyledMenuItemBase onClick={onClick}>
    {icon}
    {children}
  </StyledMenuItemBase>
);

const StyledDeleteMenuItem: React.FC<StyledMenuItemProps> = ({ onClick, icon, children }) => (
  <StyledDeleteMenuItemBase onClick={onClick}>
    {icon}
    {children}
  </StyledDeleteMenuItemBase>
);

interface StyledMenuItemNodeNameProps {
  node: TreeNode | undefined;
}
const StyledMenuItemNodeName: React.FC<StyledMenuItemNodeNameProps> = ({ node }) => {
  const theme = useTheme();
  return (
    <Box sx={{ px: theme.spacing(2), py: theme.spacing(1) }} >
      <Typography variant="subtitle2" sx={{ color: '#cccccc', fontWeight: 500 }}>
        {node?.name}
      </Typography>
      <Typography variant="caption">
        Last edited: 12.05.2025 by John Smith
      </Typography>
    </Box>
  );
};

const StyledDeleteMenuItemBase = styled(MenuItem)(() => ({
  fontSize: '13px',
  color: '#f48771',
  display: 'flex',
  alignItems: 'center',
  gap: '8px',
  '&:hover': {
    backgroundColor: '#3c3c3c',
  },
  '& .MuiSvgIcon-root': {
    color: '#f48771',
    fontSize: '16px',
  },
}));

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  marginTop: '5px !important',
  '& .MuiInputBase-root': {
    backgroundColor: '#1e1e1e',
    color: '#cccccc',
    ...theme.typography.caption,
    borderRadius: 0,
    '& fieldset': {
      borderColor: '#3c3c3c',
      borderRadius: 0,
    },
    '&:hover fieldset': {
      borderColor: '#555555',
    },
    '&.Mui-focused fieldset': {
      borderColor: theme.palette.primary.main,
    },
  },
  '& .MuiInputBase-input': {
    color: '#cccccc',
    padding: '0px',
    '&::placeholder': {
      color: '#888888',
      opacity: 1,
    },
  },
}));





