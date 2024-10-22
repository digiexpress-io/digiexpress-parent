import { ListItem, ListItemIcon, ListItemText } from '@mui/material';
import React, { forwardRef, useMemo } from 'react';
import { Link, useMatch } from 'react-router-dom';

interface Props {
  primary: string;
  to: string;
  icon?: React.ReactNode;
  button?: true;
  className?: string;
};

export const ListItemLink: React.FC<Props> = ({ icon, primary, button, to, className }) => {
  const renderLink = useMemo(
    // @ts-ignore
    () => forwardRef((itemProps, ref) => <Link to={to} ref={ref} {...itemProps} />),
    [to],
  );

  const match = useMatch(to);

  return (
    <li>
      <ListItem
        // @ts-ignore
        button={button}
        component={renderLink}
        selected={!!match && match.pattern.end}
        className={className}
      >
        {icon && <ListItemIcon>{icon}</ListItemIcon>}
        <ListItemText primary={primary}/>
      </ListItem>
    </li>
  );
}
