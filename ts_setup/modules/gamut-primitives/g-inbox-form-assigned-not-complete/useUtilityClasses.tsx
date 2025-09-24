import { alpha, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GInboxFormAssignedNotComplete';

export interface GInboxFormAssignedNotCompleteClasses {
  root: string,
  form: string,
  formIcon: string,
  formAvatar: string,
  formTitle: string,
  content: string
}
export type GInboxFormAssignedNotCompleteClassKey = keyof GInboxFormAssignedNotCompleteClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    formItem: ['formItem'],
    formIcon: ['formIcon'],
    formAvatar: ['formAvatar'],
    formTitle: ['formTitle'],
    closeButton: ['closeButton'],
    content: ['content']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GInboxFormAssignedNotCompleteRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.formItem,
      styles.formIcon,
      styles.formAvatar,
      styles.formTitle,
      styles.closeButton,
      styles.content
    ];
  },
})(({ theme }) => {
  return {
    marginTop: theme.spacing(0.5),

    '.GInboxFormAssignedNotComplete-formItem': {
      maxWidth: '30ch',
      backgroundColor: alpha(theme.palette.info.main, 0.1),
      ':hover': {
        backgroundColor: alpha(theme.palette.info.main, 0.2),
      }
    },
    '.GInboxFormAssignedNotComplete-formAvatar': {
      backgroundColor: theme.palette.info.main
    },
    '.GInboxFormAssignedNotComplete-formIcon': {
      fontSize: '15px',
      color: theme.palette.background.default
    },
    '.GInboxFormAssignedNotComplete-formTitle': {
      ...theme.typography.h1
    },
    '.GInboxFormAssignedNotComplete-closeButton': {
      marginBottom: theme.spacing(2),
      width: '20ch'
    },
    '.GInboxFormAssignedNotComplete-content': {


    },

  };
});

