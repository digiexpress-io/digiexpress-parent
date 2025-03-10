import React from 'react';
import { Box, Typography, useTheme } from '@mui/material';

import * as Burger from '@/burger';
import { useIntl } from 'react-intl';


export interface MenuItemProps {
  id: string;
  to?: string | undefined;
  icon: React.ReactNode;
  onClick?: () => void;
}

export const MenuItem: React.FC<MenuItemProps> = (props) => {
  const intl = useIntl();
  const theme = useTheme();
  return (
    <Burger.TreeItemRoot
      itemId={props.id}
      onClick={props.onClick}
      label={
        <Box display='flex' marginTop={1}>
          <Box display='flex' alignItems='center' marginRight={1} color={theme.palette.primary.light}>{props.icon}</Box>
          <Typography align="left" width='100%' noWrap={true} variant="body1" sx={{ fontWeight: "inherit", flexGrow: 1 }}>
            {intl.formatMessage({ id: props.id })}
          </Typography>
        </Box>
      }
    />
  );
}