import { styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

/** ---- ArticleDelete ---- */
export const ArticleDeleteClassName = 'ArticleDelete';

export interface ArticleDeleteClasses {
  root: string;
  infoBox: string;
  label: string;
  value: string;
  description: string;
}
export type ArticleDeleteClassKey = keyof ArticleDeleteClasses;

export const useArticleDeleteUtilityClasses = () => {
  const slots = {
    root: ['root'],
    infoBox: ['infoBox'],
    label: ['label'],
    value: ['value'],
    description: ['description'],
  };
  const getUtilityClass = (slot: string) =>
    generateUtilityClass(ArticleDeleteClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const ArticleDeleteRoot = styled('div', {
  name: ArticleDeleteClassName,
  slot: 'Root',
  overridesResolver: (props, styles) => [
    styles.root,
    styles.infoBox,
    styles.label,
    styles.value,
    styles.description,
    ],
})(({ theme }) => ({

    '& .ArticleDelete-description': {
        marginBottom: theme.spacing(1),
    },
    '& .ArticleDelete-infoBox': {
        padding: theme.spacing(1),
        borderRadius: theme.shape.borderRadius,
        backgroundColor: theme.palette.action.hover,
        wordBreak: 'break-word',
        fontFamily: 'monospace',
    },
    '& .ArticleDelete-label': {
        fontWeight: 700,
    },
    '& .ArticleDelete-value': {},
}));
