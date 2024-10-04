import React from 'react';
import { styled, Breakpoints } from '@mui/material';



export interface GFlexHiddenProps {
  children: React.ReactNode;
  hiddenOn: (br: Breakpoints) => string;
}
export const GFlexHidden: React.FC<GFlexHiddenProps> = (props) => {
  const { children } = props;
  const ownerState = {
    ...props
  }
  return (
    <GFlexHiddenRoot ownerState={ownerState} >
      {children}
    </GFlexHiddenRoot>
  )
}

const GFlexHiddenRoot = styled('span', {
  name: 'GFlexHidden',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GFlexHiddenProps }>(({ theme, ownerState }) => {
  return {
    [ownerState.hiddenOn(theme.breakpoints)]: {
      display: 'none'
    }
  };
});
