import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";


export const MUI_NAME = 'GRouterOfferSummary';

export interface GRouterOfferSummaryClasses {
  root: string,
  summaryLayout: string,
  button: string,
  spacer: string,
  title: string,
  subTitle: string,
  bodyText: string,
  icon: string
}
export type GRouterOfferSummaryClassKey = keyof GRouterOfferSummaryClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    summaryLayout: ['summaryLayout'],
    button: ['button'],
    spacer: ['spacer'],
    title: ['title'],
    subTitle: ['subTitle'],
    bodyText: ['bodyText'],
    icon: ['icon']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GRouterOfferSummaryRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.summaryLayout,
      styles.button,
      styles.spacer,
      styles.title,
      styles.subTitle,
      styles.bodyText,
      styles.icon
    ];
  },
})(({ theme }) => {
  return {
    display: 'flex',
    justifyContent: 'center',
    width: '100%',

    '.GRouterOfferSummary-summaryLayout': {
      [theme.breakpoints.up('md')]: {
        padding: theme.spacing(5),
        marginTop: theme.spacing(4),
        width: '55%',
      },
      [theme.breakpoints.down('md')]: {
        padding: theme.spacing(2),
        margin: theme.spacing(1),
        width: '85%'
      },
      display: 'flex',
      flexDirection: 'column',

      marginLeft: 'auto',
      marginRight: 'auto',
      border: `1px solid ${theme.palette.divider}`,
      backgroundColor: theme.palette.background.default
    },
    '.GRouterOfferSummary-button': {
      display: 'flex',
      justifyContent: 'center',
      marginTop: theme.spacing(2),
      width: '100%',
    },
    '.GRouterOfferSummary-button > .MuiButton-root': {
      width: '100%',
      alignSelf: 'center',
      flex: '0 0 auto',
    },
    '.GRouterOfferSummary-spacer': {
      marginTop: theme.spacing(0.5),
      marginBottom: theme.spacing(0.5)
    },
    '.GRouterOfferSummary-title': {
      ...theme.typography.h1,
      marginBottom: theme.spacing(1),
      textAlign: 'center',
    },
    '.GRouterOfferSummary-subTitle': {
      ...theme.typography.h2,
      marginBottom: theme.spacing(1)
    },
    '.GRouterOfferSummary-bodyText': {
      ...theme.typography.body1,
    },
    '.GRouterOfferSummary-icon': {
      color: theme.palette.primary.main
    },

  }
});





