import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GInboxAttachments';

export interface GInboxAttachmentsClasses {
  root: string,
  attachmentItem: string,
  attachmentAvatar: string,
  attachmentIcon: string,
  attachmentLabel: string,
}
export type GInboxAttachmentsClassKey = keyof GInboxAttachmentsClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    attachmentItem: ['attachmentItem'],
    attachmentAvatar: ['attachmentAvatar'],
    attachmentIcon: ['attachmentIcon'],
    attachmentLabel: ['attachmentLabel'],
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
      styles.attachmentLabel,
    ];
  },
})(({ theme }) => {
  return {
    marginLeft: theme.spacing(1),
    '.GInboxAttachments-attachmentItem': {
      display: 'flex',
      alignItems: 'center',
      maxWidth: '30ch',
      minWidth: 0,
      overflow: 'hidden',
      whiteSpace: 'nowrap',
      textOverflow: 'ellipsis',
      flexShrink: 1,
    },    
    '.GInboxAttachments-attachmentLabel': {
      [theme.breakpoints.up('sm')]: {
        display: 'block',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        maxWidth: '100%',
        minWidth: 0,
      },
    },    
    '.GInboxAttachments-attachmentIcon': {
      fontSize: '15px',
      color: theme.palette.error.main
    },
    '.GInboxAttachments-attachmentAvatar': {
      backgroundColor: 'unset'
    }
  }
});


