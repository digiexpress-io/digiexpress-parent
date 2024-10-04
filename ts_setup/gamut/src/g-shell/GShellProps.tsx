import React from 'react';
import { GOverridableComponent } from '../g-override';


export interface GShellClasses {
  root: string;
}

export interface GShellProps {
  children: React.ReactNode,
  drawerOpen?: boolean;

  toolbarHeight?: Partial<GShellToolbarHeightOptions>;

  footerHeight?: number;
  drawerWidth?: number;
  component?: GOverridableComponent<GShellProps>;
}


export interface GShellToolbarHeightOptions {
  xs: number,
  sm: number,
  md: number,
  lg: number,
  xl: number,
}

export type GShellClassKey = keyof GShellClasses;
