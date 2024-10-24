import { ListItemButton, ListItemIcon, Typography, useTheme } from '@mui/material';
import React, { forwardRef, useMemo } from 'react';
import { Link, useMatch } from 'react-router-dom';

interface Props {
  children: React.ReactNode;
  to: string;
  icon?: React.ReactNode;
  button?: true;
  className?: string;
};

export const ListItemLink: React.FC<Props> = ({ icon, children, button, to, className }) => {
  const theme = useTheme();

  const explorerItemColor = theme.palette.explorerItem.main;

  const renderLink = useMemo(
    // @ts-ignore
    () => forwardRef((itemProps, ref) => <Link to={to} ref={ref} {...itemProps} />),
    [to],
  );

  const match = useMatch(to);
  //useRouteId

  return (

    <ListItemButton
      // @ts-ignore
      button={button}
      component={renderLink}
      selected={!!match && match.pattern.end}
      className={className}
    >
      {icon && <ListItemIcon sx={{ color: explorerItemColor }}>{icon}</ListItemIcon>}
      <Typography sx={{ color: explorerItemColor }}> {children}</Typography>
    </ListItemButton>
  );
}
