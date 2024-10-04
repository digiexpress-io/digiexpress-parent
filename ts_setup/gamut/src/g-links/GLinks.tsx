import React from 'react';
import { useDefaultProps } from '@mui/material/DefaultPropsProvider';
import { GLinksRoot, GLinksTitle, useUtilityClasses, MUI_NAME } from './useUtilityClasses'
import { GOverridableComponent } from '../g-override';

export interface GLinksClasses {
  root: string;
  header: string
}
export type GLinksClassKey = keyof GLinksClasses;


export interface GLinksProps {
  children?: React.ReactNode,
  header?: string
  component?: GOverridableComponent<GLinksProps>;
}

export const GLinks: React.FC<GLinksProps> = (initProps) => {
  const props = useDefaultProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses();
  const Root = props.component ?? GLinksRoot;

  return (
    <Root className={classes.root} ownerState={props}>
      <GLinksTitle className={classes.title}>{props.header}</GLinksTitle>
      {props.children}
    </Root>
  )
}