import React from 'react';
import { Menu, MenuItem, Divider, Typography, styled } from '@mui/material';
import {
  DeleteForever as DeleteIcon,
  Add as NewIcon,
  Edit as EditIcon,
  ContentCopy as CopyIcon,
  DriveFileRenameOutline as RenameIcon
} from '@mui/icons-material';
import { TreeNode } from './mock-tree-data';
import { getNodeColor } from './useUtilityClasses';

interface EveliTreeItemMenuProps {
  node: TreeNode | null;
  anchorPosition: { top: number; left: number } | null;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

export const EveliTreeItemMenu: React.FC<EveliTreeItemMenuProps> = ({
  node,
  anchorPosition,
  open,
  onClose,
  onExited,
}) => {

  function handleNew() {
    console.log('New:', node?.name);
    onClose();
  }

  function handleEdit() {
    console.log('Edit:', node?.name);
    onClose();
  }

  function handleCopy() {
    console.log('Copy:', node?.name);
    onClose();
  }

  function handleDelete() {
    console.log('Delete:', node?.name);
    onClose();
  }

  function handleDuplicate() {
    console.log('Duplicate:', node?.name);
    onClose();
  }

  return (
    <Menu
      open={open}
      onClose={onClose}
      anchorReference="anchorPosition"
      anchorPosition={anchorPosition || undefined}
      transformOrigin={{
        vertical: 'top',
        horizontal: 'left',
      }}
      slotProps={{
        transition: {
          onExited: onExited,
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
      <StyledMenuItemNodeName node={node} />

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

const StyledDeleteMenuItem: React.FC<StyledMenuItemProps> = ({ onClick, icon, children }) => (
  <StyledDeleteMenuItemBase onClick={onClick}>
    {icon}
    {children}
  </StyledDeleteMenuItemBase>
);

interface MenuItemNodeNameProps {
  node: TreeNode | null;
}

const StyledMenuItemNodeName: React.FC<MenuItemNodeNameProps> = ({ node }) => {
  return (
    <Typography variant="subtitle2"
      sx={{
        px: 2,
        py: 1,
        color: node ? getNodeColor(node.type) : '#cccccc',
        fontWeight: 500,
        display: 'block',
      }}
    >
      {node?.name}
    </Typography>
  );
};