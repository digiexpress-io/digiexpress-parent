import React from 'react';
import { GFlexBody } from './GFlexBody';
import { GFlexHeader } from './GFlexHeader';
import { GFlexHidden } from './GFlexHidden';
import { Breakpoints } from '@mui/system';

export interface GFlexHeaderProps {
  variant: 'header',
  children: React.ReactNode;
}

export interface GFlexBodyProps {
  variant: 'body',
  children: React.ReactNode;
}

export interface GFlexHiddenProps {
  variant: 'hidden',
  children: React.ReactNode;
  hiddenOn: (br: Breakpoints) => string;
}

export type GFlexProps = (
  GFlexHeaderProps |
  GFlexBodyProps |
  GFlexHiddenProps
)
export const GFlex: React.FC<GFlexProps> = (props) => {
  if (props.variant === 'header') {
    return (<GFlexHeader>{props.children}</GFlexHeader>);
  } else if (props.variant === 'hidden') {
    return (<GFlexHidden hiddenOn={props.hiddenOn}>{props.children}</GFlexHidden>);
  }
  return (<GFlexBody>{props.children}</GFlexBody>);
}

