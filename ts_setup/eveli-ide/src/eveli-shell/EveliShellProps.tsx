import React from 'react'
import { EveliOverridableComponent } from '../api-variants' 

export interface EveliShellClasses {
  root: string;
}

export interface EveliShellProps {
  children: React.ReactNode,
  drawerOpen?: boolean;

  toolbarHeight?: Partial<EveliShellToolbarHeightOptions>;
  footerHeight?: number;
  drawerWidth?: number;
  minibarWidth?: number;
  
  component?: EveliOverridableComponent<EveliShellProps>;
}


export interface EveliShellToolbarHeightOptions {
  xs: number,
  sm: number,
  md: number,
  lg: number,
  xl: number,
}

export type EveliShellClassKey = keyof EveliShellClasses;