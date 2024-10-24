import React from 'react';
import { Box, Typography } from '@mui/material';

import * as Burger from '@/burger';
import { useIntl } from 'react-intl';


export interface MenuItemProps {
  labelText: string;
  to?: string | undefined;
  nodeId: string;
  icon: React.ReactNode;
  onClick: () => void;
}

export const MenuItem: React.FC<MenuItemProps> = (props) => {
  const intl = useIntl();

  return (
    <Burger.TreeItemRoot
      itemId={props.nodeId}
      onClick={props.onClick}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          <Box color="link.main" sx={{ pl: 1, mr: 1 }}>{props.icon}</Box>
          <Typography align="left" maxWidth="300px" noWrap={true} variant="body2"
            sx={{ fontWeight: "inherit", flexGrow: 1 }}
          >
            {intl.formatMessage({ id: props.labelText })}
          </Typography>
        </Box>
      }
    />
  );
}