import React from 'react';
import { Menu, MenuItem, Divider, Typography, styled, useTheme } from '@mui/material';
import {
  DeleteForever as DeleteIcon,
  Add as NewIcon,
  Edit as EditIcon,
  ContentCopy as CopyIcon,
  DriveFileRenameOutline as RenameIcon
} from '@mui/icons-material';
import { TreeNode } from '../eveli-tree-api';
import { getNodeColor } from './useUtilityClasses';

interface EveliTreeItemMenuProps {
  node: TreeNode | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

export const EveliTreeItemMenu: React.FC<EveliTreeItemMenuProps> = (props) => {

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
            minWidth: 200,
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

      <StyledDeleteMenuItem onClick={handleDelete} icon={<DeleteIcon fontSize="small" />}>Delete</StyledDeleteMenuItem>
    </Menu>
  );
};

interface StyledMenuItemProps {
  onClick: () => void;
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
    <Typography variant="subtitle2"
      sx={{
        px: theme.spacing(2), py: theme.spacing(1),
        color: node ? getNodeColor(node.type) : '#cccccc',
        fontWeight: 500,
      }}
    >
      {node?.name}
    </Typography>
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





