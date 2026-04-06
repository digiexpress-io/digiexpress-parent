import React from 'react';
import { Tooltip, Badge } from '@mui/material';
import { PanelButton, OwnerState } from './useOwnerState';

export interface ContentPanelToolbarProps {
  ownerState: OwnerState;
  className: string;
}

export const ContentPanelToolbar: React.FC<ContentPanelToolbarProps> = ({ ownerState, className }) => {
  const { toolbar } = ownerState;

  const renderIcon = (button: PanelButton) => {
    const IconComponent = button.icon;
    return <IconComponent color={button.type === 'save' ? 'error' : undefined} />;
  };

  return (
    <div className={className}>
      {toolbar.buttons.map((button) => (
        <Tooltip key={button.id} title={button.tooltip} placement="left" arrow>
          <div onClick={button.onClick}
            className={`
              ${button.type === 'save' ? 'FsMain-toolbarSaveButton' : 'FsMain-toolbarButton'}
              ${button.isSelected ? ' FsMain-toolbarButtonSelected' : ''}
            `}
          >
            {button.badge ? (
              <Badge badgeContent={button.badge} color="error"
                sx={{
                  '& .MuiBadge-badge': {
                    fontSize: '10px',
                    fontWeight: 'bold',
                    height: '16px',
                    minWidth: '16px',
                  }
                }}
              >
                {renderIcon(button)}
              </Badge>
            ) : (
              renderIcon(button)
            )}
          </div>
        </Tooltip>
      ))}
    </div>
  );
};



