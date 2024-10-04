import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled } from '@mui/material';
import { GBookingItemProps } from './GBookingItem';

export const MUI_NAME = 'GBookings';


export interface GBookingsClasses {
  root: string;
  header: string;
  started: string;
  lastModified: string;
  cancel: string;
}

export type GBookingsClassKey = keyof GBookingsClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    header: ['header'],
    started: ['started'],
    lastModified: ['lastModified'],
    cancel: ['cancel']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GBookingsRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.header
    ];
  },
})(({ theme }) => {
  return {
    '& .GBookings-header': {
      fontWeight: 'bold'
    }
  };
});



export const GBookingItemRoot = styled("div", {
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
})<{ ownerState: GBookingItemProps }>(({ theme }) => {
  return {
    cursor: 'pointer',
    '& .GBookings-started': {
      fontWeight: 'bold',
      marginRight: theme.spacing(0.5)
    },
    '& .GBookings-lastModified': {
      fontWeight: 'bold',
      marginRight: theme.spacing(0.5)
    },
    '& .GBookings-cancel': {
      color: theme.palette.error.main,
      padding: 0,
      display: 'flex',
      justifyContent: 'flex-end'
    },
  };
});