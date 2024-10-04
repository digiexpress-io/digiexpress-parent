import { generateUtilityClass, Grid, lighten, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GInbox';

export interface GInboxClasses {
  root: string,

  itemRoot: string,
  itemText: string,
  itemTitle: string,
  itemSubTitle: string,
  itemLayout: string,
  itemSentAt: string,

}
export type GInboxClassKey = keyof GInboxClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],

    itemRoot: ['itemRoot'],
    itemText: ['itemText'],
    itemTitle: ['itemTitle'],
    itemSubTitle: ['itemSubTitle'],
    itemLayout: ['itemLayout'],
    itemSentAt: ['itemSentAt']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GInboxRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,

      styles.itemRoot,
      styles.itemText,
      styles.itemTitle,
      styles.itemSubTitle,
      styles.itemLayout,
      styles.itemSentAt
    ];
  },
})(({ theme }) => {
  return {

    '.GInbox-itemRoot': {
      cursor: 'pointer',
      alignItems: 'center',
      padding: theme.spacing(2),
      backgroundColor: `${lighten(theme.palette.action.disabled, 0.85)}`,
      borderWidth: '1px',
      borderBottomStyle: 'solid',
      borderBottomColor: lighten(theme.palette.action.disabled, 0.5),
      ':hover': {
        backgroundColor: `${lighten(theme.palette.action.disabled, 0.7)}`,
        borderColor: 'rgba(194,190,194,1)',
        boxShadow: '0px 7px 5px -3px rgba(194,190,194,0.7)',
      },
    },
    '.GInbox-itemText': {
      overflow: "hidden",
      textOverflow: "ellipsis",
      maxWidth: '80rem',
      whiteSpace: 'pre',
      textAlign: 'left',
    },
    '.GInbox-itemTitle': {
      fontWeight: 'bold',
      fontSize: theme.typography.body2.fontSize
    },
    '.GInbox-itemSubTitle': {
      fontSize: theme.typography.body2.fontSize
    },
    '.GInbox-itemLayout': {
      display: 'flex',
      flexDirection: 'row',
      flexWrap: 'wrap',
      alignItems: 'center',
      padding: theme.spacing(1)
    },

    // date positioning
    '.GInbox-itemSentAt': {
      display: 'flex',
      [theme.breakpoints.up('lg')]: {
        justifyContent: 'flex-end'
      },
      [theme.breakpoints.down('lg')]: {
        justifyContent: 'flex-start'
      }
    },
  };
});



export const GInboxItemRoot = styled(Grid, {
  name: MUI_NAME,
  slot: 'Item',
  overridesResolver: (_props, styles) => {
    return [
      styles.itemRoot,
    ];
  },
})(({ theme }) => {

  return {

  };
});

