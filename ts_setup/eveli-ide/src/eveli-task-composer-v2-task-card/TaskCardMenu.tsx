import React from 'react';
import { Menu, MenuItem } from '@mui/material';
import { useIntl } from 'react-intl';


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

export interface CardMenuProps {
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


export const TaskCardMenu: React.FC<CardMenuProps> = (props) => {
  const intl = useIntl();
  const config = CARD_MENU_CONFIG[props.cardId] ?? CARD_MENU_CONFIG.default;

  function handleFlashyToggle() {
    if (props.onToggleFlashy) {
      props.onToggleFlashy()
    };
    props.onClose();
  };

  function handleReview() {
    if (props.onReview) {
      props.onReview()
    };
    props.onClose();
  };

  function handleEdit() {
    if (props.onEdit) {
      props.onEdit()
    };
    props.onClose();
  };

  return (
    <Menu
      anchorEl={props.anchorEl}
      open={props.open}
      onClose={props.onClose}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      transformOrigin={{ vertical: 'top', horizontal: 'right' }}
    >
      {config.showReview && (
        <MenuItem onClick={handleReview}>{intl.formatMessage({ id: 'taskcard.menu.option.formReview', defaultMessage: 'Show form review' })}</MenuItem>
      )}

      {config.showEdit && (
        <MenuItem onClick={handleEdit}>{intl.formatMessage({ id: 'taskcard.menu.option.editSection', defaultMessage: 'Edit this section' })}</MenuItem>
      )}

      {config.showFlashyToggle && (
        <MenuItem onClick={handleFlashyToggle}>
          {props.flashy ? (
            intl.formatMessage({ id: 'taskcard.menu.option.removeFlashy', defaultMessage: 'Remove Flashy' })
          ) : (
            intl.formatMessage({ id: 'taskcard.menu.option.setFlashy', defaultMessage: 'Make Flashy' })
          )
          }
        </MenuItem>
      )}
    </Menu>
  );
};
