import React from 'react';
import { styled, List, useThemeProps, Typography } from '@mui/material';


/**
 *  MUI theme TYPE integration
 */
export interface GSearchListClasses {
  root: string;
  subheader: string;
}
export type GSearchListClassKey = keyof GSearchListClasses;
export interface GSearchListProps {
  children?: React.ReactNode;
  subheader?: React.ReactNode;
}

/**
 * Combines styles with data + material props overrides
 */
export const GSearchList: React.FC<GSearchListProps> = (initProps) => {
  const themeProps = useThemeProps({
    props: initProps,
    name: 'GSearchList',
  });


  return (
    <GSearchListRoot disablePadding dense subheader={themeProps.subheader ? <GSearchSubheader>{themeProps.subheader}</GSearchSubheader> : undefined}>
      {themeProps.children}
    </GSearchListRoot>)
}


const GSearchSubheader = styled(Typography, {
  name: 'GSearchList',
  slot: 'Subheader',

  overridesResolver: (_props, styles) => {
    return [
      styles.subheader,
    ];
  },
})(({ theme }) => {
  return {
    ...theme.typography.h3
  };
});


const GSearchListRoot = styled(List, {
  name: 'GSearchList',
  slot: 'Root',

  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {

  };
});
