import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GInboxAttachments';

export interface GInboxAttachmentsClasses {
  root: string,
  attachmentItem: string,
  attachmentAvatar: string,
  attachmentIcon: string,
}
export type GInboxAttachmentsClassKey = keyof GInboxAttachmentsClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    attachmentItem: ['attachmentItem'],
    attachmentAvatar: ['attachmentAvatar'],
    attachmentIcon: ['attachmentIcon'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GInboxAttachmentsRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.attachmentItem,
      styles.attachmentAvatar,
      styles.attachmentIcon,
    ];
  },
})(({ theme }) => {
  return {
    margin: theme.spacing(0.5),
    '.GInboxAttachments-attachmentItem': {
      maxWidth: '25ch',
    },
    '.GInboxAttachments-attachmentIcon': {
      fontSize: '15px',
      color: theme.palette.error.main
    },
    '.GInboxAttachments-attachmentAvatar': {
      backgroundColor: 'unset'
    },


  };
});


