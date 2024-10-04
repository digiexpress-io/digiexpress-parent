import { Dialog, generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { GConfirmProps } from './GConfirm';


export const MUI_NAME = 'GConfirm';

export interface GConfirmClasses {
  root: string;
  delete: string
  title: string
  cancelItem: string
  cancelItemMeta: string
  content: string
}

export type GConfirmClassKey = keyof GConfirmClasses;

export const useUtilityClasses = (ownerState: GConfirmProps) => {
  const slots = {
    root: ['root'],
    delete: ['delete'],
    title: ['title'],
    cancelItem: ['cancelItem'],
    cancelItemMeta: ['cancelItemMeta'],
    content: ['content']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}



export const GConfirmRoot = styled(Dialog, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.delete,
      styles.title,
      styles.cancelItem,
      styles.cancelItemMeta,
      styles.content
    ];
  },
})<{ ownerState: GConfirmProps }>(({ theme }) => {
  return {
    '& .MuiPaper-root': {
      minHeight: '30%',
      maxHeight: '40%',
    },
    '& .GConfirm-delete': {
      backgroundColor: theme.palette.error.main,
      color: theme.palette.error.contrastText,
      ':hover': {
        backgroundColor: theme.palette.error.dark,
      }
    },
    '& .GConfirm-title': {
      ...theme.typography.h3,
    },
    '& .GConfirm-cancelItem': {
      ...theme.typography.h4,
    },
    '& .GConfirm-content': {
      alignContent: 'center'
    },
    '& .GConfirm-cancelItemMeta': {
      fontSize: theme.typography.body2.fontSize,
      marginBottom: theme.spacing(3)
    }

  };
});