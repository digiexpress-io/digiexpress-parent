import { alpha, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GInboxFormReview';

export interface GInboxFormReviewClasses {
  root: string,
  formReview: string,
  formReviewIcon: string,
  formReviewAvatar: string,
}
export type GInboxFormReviewClassKey = keyof GInboxFormReviewClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    reviewItem: ['reviewItem'],
    reviewIcon: ['reviewIcon'],
    reviewAvatar: ['reviewAvatar'],
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
  };
});

