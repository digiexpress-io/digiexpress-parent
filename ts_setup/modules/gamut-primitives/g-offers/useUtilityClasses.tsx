import { alpha, darken, generateUtilityClass, lighten, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GOfferItemProps } from './GOfferItem';
import { GOffersProps } from './GOffers';

export const MUI_NAME = 'GOffers';

export interface GOffersClasses {
  root: string;
  started: string;
  lastModified: string;
  cancel: string;
  noOffers: string;
  assigned: string;
  assignedIndicator: string;
}

export type GOffersClassKey = keyof GOffersClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    started: ['started'],
    lastModified: ['lastModified'],
    cancel: ['cancel'],
    header: ['header'],
    noOffers: ['noOffers'],
    assigned: ['assigned'],
    assignedIndicator: ['assignedIndicator'],
    assignedIcon: ['assignedIcon']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GOfferItemRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Item',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.started,
      styles.lastModified,
      styles.cancel,
      styles.header,
      styles.assignedIcon
    ];
  },
})<{ ownerState: GOfferItemProps }>(({ theme, ownerState }) => {


  return {

    cursor: 'pointer',

    '& .GOffers-started': {
      fontWeight: 'bold',
      marginRight: theme.spacing(0.5)
    },

    '& .GOffers-assignedIcon': {
      marginRight: theme.spacing(1),
      color: theme.palette.error.main,
    },

    '& .GOffers-assigned .MuiTypography-subtitle1': {
      color: theme.palette.error.main
    },
    '& .GOffers-assignedIndicator': {
      marginTop: theme.spacing(0.5),
      alignItems: 'center',
      display: 'flex',
      borderRadius: theme.spacing(1),
      border: `2px solid ${alpha(theme.palette.warning.main, 0.9)}`,
      backgroundColor: alpha(theme.palette.warning.light, 0.1),
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
      width: 'fit-content'
    },
    '& .GOffers-lastModified': {
      fontWeight: 'bold',
      marginRight: theme.spacing(0.5)
    },
    '& .GOffers-cancel': {
      color: theme.palette.error.main,
      padding: 0
    },
    '& .GOffers-header': {
      fontWeight: 'bold',
      fontVariant: 'h1'
    },

  };
});


export const GOffersRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.header,
      styles.noOffers
    ];
  },
})<{ ownerState: GOffersProps }>(({ theme }) => {
  return {
    '& .GOffers-noOffers.MuiPaper-root.MuiAlert-root': {
      margin: theme.spacing(1),
      padding: theme.spacing(1),
    },
    '& .GSort-root': {
      display: 'flex',
      width: '100%',
      marginBottom: theme.spacing(1),
      [theme.breakpoints.down('sm')]: {
        justifyContent: 'center',
      },
      [theme.breakpoints.up('sm')]: {
        justifyContent: 'flex-end',
      }
    }
  };
});
