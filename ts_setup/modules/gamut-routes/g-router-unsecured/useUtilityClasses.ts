import { generateUtilityClass, styled, Theme, SxProps } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { GRouterUnsecuredProps } from "./GRouterUnsecured";


export const MUI_NAME = 'GRouterUnsecured';


export interface GRouterUnsecuredClasses {
  root: string;
}
export type GRouterUnsecuredClassKey = keyof GRouterUnsecuredClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    menuButtonContainer: ['menuButtonContainer']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GRouterUnsecuredRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})<{ ownerState: GRouterUnsecuredProps }>(({ theme, ownerState }) => {

  return {
    '& .GRouterUnsecured-menuButtonContainer': {
      [theme.breakpoints.down('md')]: {
        justifyContent: 'center',
        gap: theme.spacing(1),
      },
      position: 'relative',
      backgroundImage: `url(${ownerState.backgroundImage})`,
      backgroundSize: 'cover',
      alignContent: 'center',
      display: 'flex',
      flexWrap: 'wrap',
      padding: 1,
      height: ownerState.height ?? 400,
      zIndex: 1,

      // Responsive overrides
      ..._responsiveImage(ownerState, theme)
    }
  }
});



function _responsiveImage(ownerState: GRouterUnsecuredProps, theme: Theme): SxProps {
  const responsive = ownerState.responsiveImages;
  if (!responsive) {
    return {};
  }

  return {

    backgroundSize: 'contain',
    backgroundRepeat: 'no-repeat',

    ...(responsive?.xs && {
      [theme.breakpoints.only('xs')]: {
        backgroundImage: `url(${responsive.xs.image})`,
        width: responsive.xs.width,
        height: responsive.xs.height,
        backgroundSize: responsive.xs.width === '100%' ? 'cover' : 'contain',
      }
    }),
    ...(responsive?.sm && {
      [theme.breakpoints.only('sm')]: {
        backgroundImage: `url(${responsive.sm.image})`,
        width: responsive.sm.width,
        height: responsive.sm.height,
        backgroundSize: responsive.sm.width === '100%' ? 'cover' : 'contain',
      }
    }),
    ...(responsive?.md && {
      [theme.breakpoints.only('md')]: {
        backgroundImage: `url(${responsive.md.image})`,
        width: responsive.md.width,
        height: responsive.md.height,
        backgroundSize: responsive.md.width === '100%' ? 'cover' : 'contain',
      }
    }),
    ...(responsive?.lg && {
      [theme.breakpoints.only('lg')]: {
        backgroundImage: `url(${responsive.lg.image})`,
        width: responsive.lg.width,
        height: responsive.lg.height,
        backgroundSize: responsive.lg.width === '100%' ? 'cover' : 'contain',
      }
    }),
    ...(responsive?.xl && {
      [theme.breakpoints.up('xl')]: {
        backgroundImage: `url(${responsive.xl.image})`,
        width: responsive.xl.width,
        height: responsive.xl.height,
        backgroundSize: responsive.xl.width === '100%' ? 'cover' : 'contain',
      }
    }),
  }
}

