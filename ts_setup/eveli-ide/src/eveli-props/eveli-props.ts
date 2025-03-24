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