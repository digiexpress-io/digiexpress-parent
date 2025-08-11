import { CSSInterpolation, CSSObject, Interpolation, Theme } from '@mui/material';
import { OverridesStyleRules } from '@mui/material/styles/overrides';

import { EveliLogoClassKey, EveliLogoProps } from '../eveli-logo';


/**
 * MUI theme integration
 */
export interface EveliComponentsPropsList {
  EveliLogo: EveliLogoProps;
}

export interface EveliComponentNameToClassKey {
  EveliLogo: EveliLogoClassKey;
}

export interface EveliComponents<Theme = unknown> {
  EveliLogo?: {
    defaultProps?: EveliComponentsProps['EveliLogo'];
    styleOverrides?: EveliComponentsOverrides<Theme>['EveliLogo'];
    variants?: EveliComponentsVariants['EveliLogo'];
  }
}


/**
 * MUI module overrides 
 */
export type EveliComponentsProps = {
  [Name in keyof EveliComponentsPropsList]?: Partial<EveliComponentsPropsList[Name]>;
};

export type EveliComponentsVariants = {
  [Name in keyof EveliComponentsPropsList]?: Array<{
    props: Partial<EveliComponentsPropsList[Name]>;
    style: Interpolation<{ theme: Theme }>;
  }>;
}
export type EveliComponentsOverrides<Theme = unknown> = {
  [Name in keyof EveliComponentNameToClassKey]?: Partial<
    OverridesStyleRules<EveliComponentNameToClassKey[Name], Name, Theme>
  >;
} & {
  MuiCssBaseline?: CSSObject | string | ((theme: Theme) => CSSInterpolation);
}


// Register eveli components
declare module '@mui/material' {
  export interface Components<Theme = unknown> extends EveliComponents<Theme> { }
}

declare module '@mui/material/Button' {
  interface ButtonPropsVariantOverrides {
    explorerInactive: true;
    explorerActive: true;
  }
}

declare module 'react' {
  interface CSSProperties {
    '--tree-view-text-color'?: string;
    '--tree-view-color'?: string;
    '--tree-view-bg-color'?: string;
    '--tree-view-hover-color'?: string;
  }
}
