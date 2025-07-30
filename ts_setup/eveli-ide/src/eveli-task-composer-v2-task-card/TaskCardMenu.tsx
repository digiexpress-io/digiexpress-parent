import React from 'react';
import { Menu, MenuItem } from '@mui/material';

interface TaskCardMenuProps {
  cardId: string;
  anchorEl: HTMLElement | null;
  open: boolean;
  flashy?: boolean;
  onClose: () => void;
  onToggleFlashy?: () => void;
  onReview?: () => void;
  onEdit?: () => void;
}

interface CardMenuOptions {
  showFlashyToggle?: boolean;
  showReview?: boolean;
  showEdit?: boolean;
}

const CARD_MENU_CONFIG: Record<string, CardMenuOptions> = {
  'task_main': {
    showFlashyToggle: true,
    showEdit: true
  },
  'task-form-summary': {
    showFlashyToggle: true,
    showReview: true
  },
  'notes': {
    showFlashyToggle: true,
    showEdit: true
  },
  'files': {
    showFlashyToggle: true,
    showEdit: true
  },
  'status-priority': {
    showFlashyToggle: true
  },
  'assignees-roles': {
    showFlashyToggle: true
  },
  'customer_messages': {
    showFlashyToggle: true,
    showEdit: true
  },
  'feedback': {
    showFlashyToggle: true,
    showEdit: true
  },
  'task-meta': {
    showFlashyToggle: true
  },
  default: {
    showFlashyToggle: true
  },
};

export const TaskCardMenu: React.FC<TaskCardMenuProps> = ({
  cardId,
  anchorEl,
  open,
  onClose,
  flashy,
  onToggleFlashy,
  onReview,
  onEdit,
}) => {
  const config = CARD_MENU_CONFIG[cardId] ?? CARD_MENU_CONFIG.default;

  const handleFlashyToggle = () => {
    if (onToggleFlashy) onToggleFlashy();
    onClose();
  };

  const handleReview = () => {
    if (onReview) onReview();
    onClose();
  };

  const handleEdit = () => {
    if (onEdit) {
      onEdit()
    };
    onClose();
  };

  return (
    <Menu
      anchorEl={anchorEl}
      open={open}
      onClose={onClose}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      transformOrigin={{ vertical: 'top', horizontal: 'right' }}
    >
      {config.showFlashyToggle && (
        <MenuItem onClick={handleFlashyToggle}>
          {flashy ? 'Remove Flashy' : 'Make Flashy'}
        </MenuItem>
      )}

      {config.showReview && (
        <MenuItem onClick={handleReview}>Form Review</MenuItem>
      )}

      {config.showEdit && (
        <MenuItem onClick={handleEdit}>Edit this section</MenuItem>
      )}
    </Menu>
  );
};
