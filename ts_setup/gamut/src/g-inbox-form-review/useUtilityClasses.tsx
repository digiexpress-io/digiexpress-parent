import { alpha, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GInboxFormReview';

export interface GInboxFormReviewClasses {
  root: string,
  review: string,
  reviewIcon: string,
  reviewAvatar: string,
  reviewTitle: string,
  content: string
}
export type GInboxFormReviewClassKey = keyof GInboxFormReviewClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    reviewItem: ['reviewItem'],
    reviewIcon: ['reviewIcon'],
    reviewAvatar: ['reviewAvatar'],
    reviewTitle: ['reviewTitle'],
    closeButton: ['closeButton'],
    content: ['content']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GInboxFormReviewRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.reviewItem,
      styles.reviewIcon,
      styles.reviewAvatar,
      styles.reviewTitle,
      styles.closeButton,
      styles.content
    ];
  },
})(({ theme }) => {
  return {
    margin: theme.spacing(0.5),

    '.GInboxFormReview-reviewItem': {
      maxWidth: '25ch',
      ':hover': {
        backgroundColor: alpha(theme.palette.warning.main, 0.4),
      }
    },
    '.GInboxFormReview-reviewAvatar': {
      backgroundColor: theme.palette.warning.main
    },
    '.GInboxFormReview-reviewIcon': {
      fontSize: '15px',
      color: alpha(theme.palette.text.primary, 0.9)
    },
    '.GInboxFormReview-reviewTitle': { //TODO
      ...theme.typography.h1
    },
    '.GInboxFormReview-closeButton': {
      marginBottom: theme.spacing(2),
      width: '20ch'
    },
    '.GInboxFormReview-content': {


    },

  };
});

