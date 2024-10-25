import React from 'react';
import { Box, Typography } from '@mui/material';

import * as Burger from '@/burger';
import { useIntl } from 'react-intl';


export interface MenuItemProps {
  id: string;
  to?: string | undefined;
  icon: React.ReactNode;
  onClick: () => void;
}

export const MenuItem: React.FC<MenuItemProps> = (props) => {
  const intl = useIntl();

  return (
    <Burger.TreeItemRoot
      itemId={props.id}
      onClick={props.onClick}
      label={
        <Box display='flex' marginTop={1}>
          <Box display='flex' alignItems='center' marginRight={1} color="link.main">{props.icon}</Box>
          <Typography align="left" maxWidth="300px" noWrap={true} variant="body1" sx={{ fontWeight: "inherit", flexGrow: 1 }}>
            {intl.formatMessage({ id: props.id })}
          </Typography>
        </Box>
      }
    />
  );
}