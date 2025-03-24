import React from 'react';
import { styled, Breakpoints } from '@mui/material';



export interface EveliFlexHiddenProps {
  children: React.ReactNode;
  hiddenOn: (br: Breakpoints) => string;
}
export const EveliFlexHidden: React.FC<EveliFlexHiddenProps> = (props) => {
  const { children } = props;
  const ownerState = {
    ...props
  }
  return (
    <EveliFlexHiddenRoot ownerState={ownerState} >
      {children}
    </EveliFlexHiddenRoot>
  )
}

const EveliFlexHiddenRoot = styled('span', {
  name: 'EveliFlexHidden',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: EveliFlexHiddenProps }>(({ theme, ownerState }) => {
  return {
    [ownerState.hiddenOn(theme.breakpoints)]: {
      display: 'none'
    }
  };
});
