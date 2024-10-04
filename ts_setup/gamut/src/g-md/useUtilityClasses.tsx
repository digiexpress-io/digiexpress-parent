
import { generateUtilityClass, styled } from '@mui/material';
import { GMarkdownProps } from './GMarkdown';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GMarkdown';

export interface GMarkdownClasses {
  root: string;
}
export type GMarkdownClassKey = keyof GMarkdownClasses;


export const GMarkdownRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})<{ ownerState: GMarkdownProps }>(({ theme }) => {
  return {
  };
});

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

