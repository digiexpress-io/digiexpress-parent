import React from 'react';
import { EveliFlexBody } from './EveliFlexBody';
import { EveliFlexHeader } from './EveliFlexHeader';
import { EveliFlexHidden } from './EveliFlexHidden';
import { Breakpoints } from '@mui/system';

export interface EveliFlexHeaderProps {
  variant: 'header',
  children: React.ReactNode;
}

export interface EveliFlexBodyProps {
  variant: 'body',
  children: React.ReactNode;
}

export interface EveliFlexHiddenProps {
  variant: 'hidden',
  children: React.ReactNode;
  hiddenOn: (br: Breakpoints) => string;
}

export type EveliFlexProps = (
  EveliFlexHeaderProps |
  EveliFlexBodyProps |
  EveliFlexHiddenProps
)
export const EveliFlex: React.FC<EveliFlexProps> = (props) => {
  if (props.variant === 'header') {
    return (<EveliFlexHeader>{props.children}</EveliFlexHeader>);
  } else if (props.variant === 'hidden') {
    return (<EveliFlexHidden hiddenOn={props.hiddenOn}>{props.children}</EveliFlexHidden>);
  }
  return (<EveliFlexBody>{props.children}</EveliFlexBody>);
}

