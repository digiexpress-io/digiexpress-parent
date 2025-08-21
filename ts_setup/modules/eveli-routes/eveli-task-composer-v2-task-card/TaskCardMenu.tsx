import React from 'react';
import { Menu, MenuItem } from '@mui/material';
import { useIntl } from 'react-intl';
import { useTaskDashboard } from '../eveli-task-composer-v2';


const CARD_MENU_CONFIG: Record<string, CardMenuOptions> = {
  'task_main': {
    showFlashyToggle: true,
    showEdit: true,
    showAltViewToggle: true
  },
  'task_main_alt': {
    showFlashyToggle: false,
    showEdit: true,
    showAltViewToggle: false
  },
  'task_form_summary': {
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
  'status_priority': {
    showFlashyToggle: true,
    showEdit: true
  },
  'assignees_roles': {
    showFlashyToggle: true,
    showEdit: true
  },
  'customer_messages': {
    showFlashyToggle: true,
    showEdit: true
  },
  'feedback': {
    showFlashyToggle: true,
    showEdit: true
  },
  'task_meta': {
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
  altView?: boolean;
  onClose: () => void;
  onToggleFlashy?: () => void;
  onToggleAltView?: () => void;
  onReview?: () => void;
  onEdit?: () => void;
}

interface CardMenuOptions {
  showFlashyToggle?: boolean;
  showAltViewToggle?: boolean;
  showReview?: boolean;
  showEdit?: boolean;
}


export const TaskCardMenu: React.FC<CardMenuProps> = (props) => {
  const intl = useIntl();
  const config = CARD_MENU_CONFIG[props.cardId] ?? CARD_MENU_CONFIG.default;
  const { task } = useTaskDashboard();

  const isTaskReopenable = task.status === 'COMPLETED' || task.status === 'REJECTED' && task.questionnaireId;


  function handleFlashyToggle() {
    if (props.onToggleFlashy) {
      props.onToggleFlashy()
    };
    props.onClose();
  };

  function handleAltViewToggle() {
    if (props.onToggleAltView) {
      props.onToggleAltView()
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

      {config.showAltViewToggle && (
        <MenuItem onClick={handleAltViewToggle}>
          {props.altView ? (
            intl.formatMessage({ id: 'taskcard.menu.option.removeAltView', defaultMessage: 'Remove alternative view' })
          ) : (
            intl.formatMessage({ id: 'taskcard.menu.option.setAltView', defaultMessage: 'Change to alternative view' })
          )
          }
        </MenuItem>
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
