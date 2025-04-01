import { generateUtilityClass, Grid, lighten, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GInbox';

export interface GInboxClasses {
  root: string,
  itemRoot: string,
  itemTitle: string,
  itemSubTitle: string,
  itemLayout: string,
  itemSentAt: string,
  itemAttachments: string,
  taskRefLayout: string,
  newMsgIndicator: string
}
export type GInboxClassKey = keyof GInboxClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    itemRoot: ['itemRoot'],
    itemTitle: ['itemTitle'],
    itemSubTitle: ['itemSubTitle'],
    itemLayout: ['itemLayout'],
    itemSentAt: ['itemSentAt'],
    itemAttachments: ['itemAttachments'],
    taskRefLayout: ['taskRefLayout'],
    newMsgIndicator: ['newMsgIndicator']

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
      styles.itemTitle,
      styles.itemSubTitle,
      styles.itemLayout,
      styles.itemSentAt,
      styles.itemAttachments,
      styles.taskRefLayout,
      styles.newMsgIndicator
    ];
  },
})(({ theme }) => {
  return {

    '.GInbox-itemRoot': {
      cursor: 'pointer',
      alignItems: 'center',
      paddingLeft: theme.spacing(2),
      paddingRight: theme.spacing(2),
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
    '.GInbox-itemTitle': {
      fontSize: theme.typography.body2.fontSize,
      [theme.breakpoints.down('sm')]: {
        textAlign: 'right',
      }
    },
    '.GInbox-itemSubTitle': {
      fontSize: theme.typography.body2.fontSize
    },
    '.GInbox-newMsgIndicator': {
      marginRight: theme.spacing(1),
      color: theme.palette.error.main,
      animation: 'pulse 1.5s ease-in-out infinite',
      transition: 'transform 0.3s ease-in-out',
    },
    '@keyframes pulse': {
      '0%': { transform: 'scale(1)', opacity: 1 },
      '50%': { transform: 'scale(1.1)', opacity: 0.8 },
      '100%': { transform: 'scale(1)', opacity: 1 },
    },
    '.GInbox-taskRefLayout': {
      display: 'flex',
      alignItems: 'center'
    },
    '.GInbox-itemLayout': {
      display: 'flex',
      flexDirection: 'row',
      flexWrap: 'wrap',
      alignItems: 'center',
    },

    // date positioning
    '.GInbox-itemSentAt': {
      display: 'flex',
      [theme.breakpoints.up('sm')]: {
        alignItems: 'flex-end',
        flexDirection: 'column'
      },
      [theme.breakpoints.down('md')]: {
        justifyContent: 'space-between',
      }
    },
    '.GInbox-itemAttachments': {
      display: 'flex',
      [theme.breakpoints.up('sm')]: {
        justifyContent: 'flex-end'
      },
      [theme.breakpoints.down('sm')]: {
        justifyContent: 'flex-start',
      }
    },
    '& .GSort-root': {
      display: 'flex',
      width: '100%',
      marginBottom: theme.spacing(1),
      [theme.breakpoints.up('sm')]: {
        justifyContent: 'flex-end'
      },
      [theme.breakpoints.down('sm')]: {
        justifyContent: 'center'
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

