import { Link, ListItem, ListItemIcon, ListItemText, IconButton, ListItemSecondaryAction } from '@mui/material';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import React, { forwardRef, useMemo } from 'react';

interface Props {
  primary: string;
  to: string;
  icon?: React.ReactNode;
  button?: true;
  className?: string;
  showEndIcon?: boolean;
};

export const ExternalLink: React.FC<Props> = ({ icon, primary, button, to, className, showEndIcon = true }) => {
  const renderLink = useMemo(
    // @ts-ignore
    () => forwardRef((itemProps, ref) => <Link target='_blank' href={to} {...itemProps} />),
    [to],
  );


  return (
    <li>
      <ListItem
        // @ts-ignore
        button={button}
        component={renderLink}
        className={className}
      >
        {icon && <ListItemIcon>{icon}</ListItemIcon>}
        <ListItemText primary={primary} />
        {showEndIcon && (<ListItemSecondaryAction>
          <IconButton edge="end" aria-label="external">
            <OpenInNewIcon />
          </IconButton>
        </ListItemSecondaryAction>)}
      </ListItem>
    </li>
  );
}
