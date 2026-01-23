import { Interpolation, Theme, CSSInterpolation, CSSObject } from '@mui/material';
import { OverridesStyleRules } from '@mui/material/styles/overrides';

export const components_g: INTERNAL_TYPE_s<Theme> = {
  GUserOverview: {
    defaultProps: {
      slotProps: {
        bookings: {
          disabled: false
        }
      }
    }
  },
  GUserOverviewMenu: {
    defaultProps: {
      slotProps: {
        bookings: {
          disabled: false
        }
      }
    }
  },

  GShell: {
    defaultProps: {
      footerHeight: 300,
      drawerWidth: 350,
      toolbarHeight: {
        xs: 155,
        sm: 150,
        md: 90,
        lg: 90,
        xl: 90
      }
    },
    styleOverrides: {
      root: ({  }) => ({
      })
    }
  },

  GMarkdown: {
    defaultProps: {
      children: "# Portal under maintainence",
    },
    styleOverrides: {
      // @ts-ignore
      root: ({ theme }) => ({
        '& .MuiTypography-h1': {
          ...theme.typography.h1
        },
        '& :is(h1, h2, h3, h4, h5, p)': {
          marginBottom: theme.spacing(2)
        }
      })
    }
  },

  GArticleFeedback: {
    defaultProps: {
      enabled(view: { id: string }) { //015_Palaute
        return view.id.toLowerCase().endsWith('palaute');
      },
    }
  },
}


/**
 * MUI types for default values, should only internal, aka default g component overrides
 */
interface INTERNAL_TYPE_NameToClassKey {
  GUserOverview: keyof { root: string },
  GUserOverviewMenu: keyof { root: string  },
  GShell: keyof { root: string },
  GMarkdown: keyof {root: string },
  GArticleFeedback: keyof {root: string }
}

interface INTERNAL_TYPE_s<Theme = unknown> {

  GUserOverview?: {
    defaultProps?: INTERNAL_TYPE_Props['GUserOverview'];
    styleOverrides?: INTERNAL_TYPE_Overrides<Theme>['GUserOverview'];
    variants?: INTERNAL_TYPE_Variants['GUserOverview'];
  },

  GUserOverviewMenu?: {
    defaultProps?: INTERNAL_TYPE_Props['GUserOverviewMenu'];
    styleOverrides?: INTERNAL_TYPE_Overrides<Theme>['GUserOverviewMenu'];
    variants?: INTERNAL_TYPE_Variants['GUserOverviewMenu'];
  },

  GShell?: {
    defaultProps?: INTERNAL_TYPE_Props['GShell'];
    styleOverrides?: INTERNAL_TYPE_Overrides<Theme>['GShell'];
    variants?: INTERNAL_TYPE_Variants['GShell'];
  },

  GMarkdown?: {
    defaultProps?: INTERNAL_TYPE_Props['GMarkdown'];
    styleOverrides?: INTERNAL_TYPE_Overrides<Theme>['GMarkdown'];
    variants?: INTERNAL_TYPE_Variants['GMarkdown'];
  },

  GArticleFeedback?: {
    defaultProps?: INTERNAL_TYPE_Props['GArticleFeedback'];
    styleOverrides?: INTERNAL_TYPE_Overrides<Theme>['GArticleFeedback'];
    variants?: INTERNAL_TYPE_Variants['GArticleFeedback'];
  },
}

interface INTERNAL_TYPE_PropsList {
  GUserOverview: {},
  GUserOverviewMenu: {},
  GShell: {},
  GMarkdown: {},
  GArticleFeedback: {}
}

type INTERNAL_TYPE_Props = {
  [Name in keyof INTERNAL_TYPE_PropsList]?: Partial<INTERNAL_TYPE_PropsList[Name]>;
};

type INTERNAL_TYPE_Variants = {
  [Name in keyof INTERNAL_TYPE_PropsList]?: Array<{
    props: Partial<INTERNAL_TYPE_PropsList[Name]>;
    style: Interpolation<{ theme: Theme }>;
  }>;
}
type INTERNAL_TYPE_Overrides<Theme = unknown> = {
  [Name in keyof INTERNAL_TYPE_NameToClassKey]?: Partial<
    OverridesStyleRules<INTERNAL_TYPE_NameToClassKey[Name], Name, Theme>
  >;
} & {
  MuiCssBaseline?: CSSObject | string | ((theme: Theme) => CSSInterpolation);
}