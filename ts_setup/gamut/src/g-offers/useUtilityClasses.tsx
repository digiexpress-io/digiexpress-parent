import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GOfferItemProps } from './GOfferItem';
import { GOffersProps } from './GOffers';


export const MUI_NAME = 'GOffers';

export interface GOffersClasses {
  root: string;
  started: string;
  lastModified: string;
  cancel: string;
}

export type GOffersClassKey = keyof GOffersClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    started: ['started'],
    lastModified: ['lastModified'],
    cancel: ['cancel'],
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
    ];
  },
})<{ ownerState: GOfferItemProps }>(({ theme, ownerState }) => {
  return {
    cursor: 'pointer',
    '& .GOffers-started': {
      fontWeight: 'bold',
      marginRight: theme.spacing(0.5)
    },
    '& .GOffers-lastModified': {
      fontWeight: 'bold',
      marginRight: theme.spacing(0.5)
    },
    '& .GOffers-cancel': {
      color: theme.palette.error.main,
      padding: 0
    },
  };
});


export const GOffersRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GOffersProps }>(({ theme }) => {
  return {
  };
});
