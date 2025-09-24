import React from 'react';
import { Menu, MenuItem } from '@mui/material';
import { useIntl } from 'react-intl';


export interface CardMenuProps {
  cardId: string;
  anchorEl: HTMLElement | null;
  open: boolean;
  flashy?: boolean;

  showFlashyToggle: boolean;
  showReview: boolean;
  showEdit: boolean;

  onClose: () => void;
  onToggleFlashy?: () => void;
  onToggleAltView?: () => void;
  onReview?: () => void;
  onEdit?: () => void;
}

export const TaskCardMenu: React.FC<CardMenuProps> = (props) => {
  const intl = useIntl();

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
      {props.showReview && (
        <MenuItem onClick={handleReview}>{intl.formatMessage({ id: 'taskcard.menu.option.formReview', defaultMessage: 'Show form review' })}</MenuItem>
      )}

      {props.showEdit && (
        <MenuItem onClick={handleEdit}>{intl.formatMessage({ id: 'taskcard.menu.option.editSection', defaultMessage: 'Edit this section' })}</MenuItem>
      )}

      {props.showFlashyToggle && (
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
