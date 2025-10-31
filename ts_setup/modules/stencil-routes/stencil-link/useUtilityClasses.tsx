import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const LinkDeleteClassName = 'LinkDelete';

export interface LinkDeleteClasses {
  root: string;
  infoBox: string;
  label: string;
  value: string;
}
export type LinkDeleteClassKey = keyof LinkDeleteClasses;

export const useLinkDeleteUtilityClasses = () => {
  const slots = {
    root: ['root'],
    infoBox: ['infoBox'],
    label: ['label'],
    value: ['value'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(LinkDeleteClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const LinkDeleteRoot = styled('div', {
  name: LinkDeleteClassName,
  slot: 'Root',
  overridesResolver: (props, styles) => [
    styles.root,
    styles.infoBox,
    styles.label,
    styles.value,
  ],
})<{}>(({ theme }) => ({
  '& .LinkDelete-infoBox': {
    padding: theme.spacing(1),
    borderRadius: theme.shape.borderRadius,
    backgroundColor: theme.palette.action.hover,
    wordBreak: 'break-all',
    fontFamily: 'monospace',
  },

  '& .LinkDelete-label': {
    fontWeight: 700,
  },

  '& .LinkDelete-value': {
  },
}));
