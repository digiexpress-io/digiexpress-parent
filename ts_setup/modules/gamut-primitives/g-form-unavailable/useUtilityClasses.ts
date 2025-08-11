import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";


export const MUI_NAME = 'GFormUnavailable';


export interface GFormUnavailableClasses {
  root: string;
  title: string;
  buttons: string
}

export type GFormUnavailableClassKey = keyof GFormUnavailableClasses;



export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    pageTitle: ['pageTitle'],
    buttons: ['buttons'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}



export const GFormUnavailableRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.title,
    ];
  },
})(({ theme }) => {
  return {
    display: 'flex',
    flexDirection: 'column'
  }
});

export const GFormUnavailableTitleSlot = styled("div", {
  name: MUI_NAME,
  slot: 'Title'
})(({ theme }) => {
  return {

    '.GFormUnavailable-pageTitle': {
      textAlign: 'center',
      marginBottom: theme.spacing(3),
      ...theme.typography.h1
    },

  }
});

export const GFormUnavailableButtonsSlot = styled("div", {
  name: MUI_NAME,
  slot: 'Buttons',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {
    gap: theme.spacing(1),
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2),
    display: 'flex',
    flexDirection: 'column',
    width: '100%'
  }
});

