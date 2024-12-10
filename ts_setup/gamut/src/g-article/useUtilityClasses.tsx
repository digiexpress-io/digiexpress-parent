import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '../api-variants';
import { GArticleProps } from './GArticle';


export const MUI_NAME = 'GArticle';

export interface GArticleClasses {
  root: string;
  page: string;
  pageLinks: string;
  pageBottom: string;
  content: string;
}

export type GArticleClassKey = keyof GArticleClasses;


export const useUtilityClasses = (ownerState: GArticleProps) => {
  const slots = {
    root: ['root'],
    content: ['content'],
    page: ['page'],
    pageLinks: ['pageLinks'],
    pageBottom: ['pageBottom']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GArticleRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {

    return [
      styles.root,
      styles.page,
      styles.pageLinks,
      styles.pageBottom,
      styles.content,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GArticleProps }>(({ theme }) => {
  return {


    '& .GArticle-content': {
      display: 'flex',
      flexGrow: 1,
      padding: theme.spacing(2),
      [theme.breakpoints.down('md')]: {
        flexDirection: 'column',
      },
    },

    '& .GArticle-page': {
      [theme.breakpoints.up('md')]: {
        width: '70%',
      },
    },
    '& .GArticle-pageLinks': {
      [theme.breakpoints.up('md')]: {
        width: '30%',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'stretch'
      },
      [theme.breakpoints.down('md')]: {
        width: '100%'
      }

    },
    '& .GArticle-pageBottom': {
      padding: theme.spacing(2),
    }
  };
});
