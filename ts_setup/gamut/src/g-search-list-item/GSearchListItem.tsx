import React from 'react';
import { styled, useThemeProps, Typography, ListItem, ListItemButton, ListItemIcon, Chip } from '@mui/material';


/**
 *  MUI theme TYPE integration
 */
export interface GSearchListItemClasses {
  root: string;
  subheader: string;
}
export type GSearchListItemClassKey = keyof GSearchListItemClasses;
export interface GSearchListItemProps {
  children?: React.ReactNode;
  label?: React.ReactNode;
  onClick?: (event: React.MouseEvent) => void;
}

/**
 * Combines styles with data + material props overrides
 */
export const GSearchListItem: React.FC<GSearchListItemProps> = (initProps) => {
  const themeProps = useThemeProps({
    props: initProps,
    name: 'GSearchListItem',
  });

  return (
    <GSearchListItemRoot>
      <ListItemButton>
        <ListItemIcon><Chip label={themeProps.label} /></ListItemIcon>
        <Typography>{themeProps.children}</Typography>
      </ListItemButton>
    </GSearchListItemRoot>)
}


const GSearchListItemRoot = styled(ListItem, {
  name: 'GSearchListItem',
  slot: 'Root',

  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {
    padding: 'unset',

    '& .MuiListItemButton-root .MuiListItemIcon-root .MuiChip-root': {
      backgroundColor: theme.palette.secondary.main,
      marginRight: theme.spacing(1),
      minWidth: '100px'
    },
  };
});
