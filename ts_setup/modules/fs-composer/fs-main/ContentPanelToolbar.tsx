import React from 'react';
import { Tooltip, Badge } from '@mui/material';
import { PanelButton, OwnerState } from './useOwnerState';
import { useUtilityClasses } from './useUtilityClasses';

export interface ContentPanelToolbarProps {
  ownerState: OwnerState;
  className: string;
}

export const ContentPanelToolbar: React.FC<ContentPanelToolbarProps> = ({ ownerState, className }) => {
  const { toolbar } = ownerState;
  const classes = useUtilityClasses({});

  function getButtonClassName(button: PanelButton): string {
    const root = button.type === 'save' ? classes.toolbarSaveButton : classes.toolbarButton;
    if (button.isSelected && !button.isEnabled) {
      return `${root} ${classes.toolbarButtonSelected} ${classes.toolbarButtonDisabled}`;
    }
    if (button.isSelected) {
      return `${root} ${classes.toolbarButtonSelected}`;
    }
    if (!button.isEnabled) {
      return `${root} ${classes.toolbarButtonDisabled}`;
    }
    return root;
  }

  return (
    <div className={className}>
      {toolbar.buttons.map((button) => (
        <Tooltip key={button.id} title={button.tooltip} placement="left" arrow>
          <div onClick={button.isEnabled ? button.onClick : undefined} className={getButtonClassName(button)}>
            {button.badge ? (
              <Badge badgeContent={button.badge} color="error" className={classes.toolbarBadge}>
                <button.icon />
              </Badge>
            ) : (
                <button.icon />
            )}
          </div>
        </Tooltip>
      ))}
    </div>
  );
};



