import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GFormReview';

export interface GFormReviewClasses {
  root: string,
}
export type GFormReviewClassKey = keyof GFormReviewClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    loader: ['loader']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GFormReviewRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.loader
    ];
  },
})(({ theme }) => {
  return {
    display: 'flex',

    '.GFormReview-loader': {

    }

  };
});

