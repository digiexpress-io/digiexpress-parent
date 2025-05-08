import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import city1 from './city1.jpg'
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
      backgroundImage: `url(${ownerState.backgroundImage ?? city1})`,
      backgroundSize: 'cover',
      alignContent: 'center',
      display: 'flex',
      flexWrap: 'wrap',
      padding: 1,
      height: ownerState.height ?? 400,
      zIndex: 1,
    }
  }
});
