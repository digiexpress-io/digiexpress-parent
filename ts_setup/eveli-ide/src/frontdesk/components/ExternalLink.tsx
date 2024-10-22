import { Link, ListItemIcon, IconButton, ListItemSecondaryAction, useTheme, ListItemButton, Typography } from '@mui/material';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import React, { forwardRef, useMemo } from 'react';

interface Props {
  children: React.ReactNode;
  to: string;
  icon?: React.ReactNode;
  button?: true;
  className?: string;
  showEndIcon?: boolean;
};

export const ExternalLink: React.FC<Props> = ({ icon, children, button, to, className, showEndIcon = true }) => {
  const theme = useTheme();

  const renderLink = useMemo(
    // @ts-ignore
    () => forwardRef((itemProps, ref) => <Link target='_blank' href={to} {...itemProps} />),
    [to],
  );

  const explorerItemColor = theme.palette.explorerItem.main;


  return (
    <ListItemButton
      // @ts-ignore
      button={button}
      component={renderLink}
      className={className}
    >
      {icon && <ListItemIcon sx={{ color: explorerItemColor }}>{icon}</ListItemIcon>}
      <Typography sx={{ color: explorerItemColor }}>{children}</Typography>
      {showEndIcon && (<ListItemSecondaryAction>
        <IconButton edge="end" aria-label="external">
          <OpenInNewIcon sx={{ color: explorerItemColor }} />
        </IconButton>
      </ListItemSecondaryAction>)}
    </ListItemButton>
  );
}
