import { generateUtilityClass, Grid, lighten, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GInbox';

export interface GInboxClasses {
  root: string,
  inboxItem: string,
  inboxItemTitle: string,
  inboxItemSentAt: string,
  inboxItemAttachments: string,
  taskRefLayout: string,
  newMsgIndicator: string
}
export type GInboxClassKey = keyof GInboxClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    inboxItem: ['inboxItem'],
    inboxItemTitle: ['inboxItemTitle'],
    inboxItemSentAt: ['inboxItemSentAt'],
    inboxItemAttachments: ['inboxItemAttachments'],
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
      styles.inboxItem,
      styles.inboxItemTitle,
      styles.inboxItemSentAt,
      styles.inboxItemAttachments,
      styles.taskRefLayout,
      styles.newMsgIndicator
    ];
  },
})(({ theme }) => {
  return {

    '.GInbox-inboxItem': {
      display: 'flex',
      flexWrap: 'nowrap',
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
      [theme.breakpoints.down('sm')]: {
        flexWrap: 'wrap',
        rowGap: theme.spacing(1),
      },
    },

    '.GInbox-inboxItemTitle': {
      fontSize: theme.typography.body2.fontSize,
      display: 'flex',
      [theme.breakpoints.down('sm')]: {
        justifyContent: 'flex-end',
        textAlign: 'right',
        flex: '1 1 0%',
        minWidth: 0,
        maxWidth: '100%',
      },
      [theme.breakpoints.only('md')]: {
        justifyContent: 'flex-end',
        textAlign: 'right',
      },
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
      alignItems: 'center',
    },

    // date positioning
    '.GInbox-inboxItemSentAt': {
      display: 'flex',
      flexShrink: 0,
      justifyContent: 'flex-end',
      alignItems: 'center',
      whiteSpace: 'nowrap',
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      paddingLeft: theme.spacing(1),
      [theme.breakpoints.down('sm')]: {
        flexDirection: 'row', 
        justifyContent: 'space-between',
        alignItems: 'center',
      }, 
      [theme.breakpoints.up('sm')]: {
        flexShrink: 0,
        width: '8ch',
        minWidth: '8ch',
        maxWidth: '8ch',
        overflow: 'hidden',
        whiteSpace: 'nowrap',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
      },
      [theme.breakpoints.up('md')]: {
        justifyContent: 'space-between',
        textAlign: 'right',
      },
      [theme.breakpoints.up('lg')]: {
        justifyContent: 'flex-end',
        textAlign: 'right',
      },      
    },

    '.GInbox-inboxItemAttachments': {
      [theme.breakpoints.up('sm')]: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexGrow: 1,
        flexShrink: 1,
        minWidth: 0,
        overflow: 'hidden',
      },
      [theme.breakpoints.only('lg')]: {
        justifyContent: 'flex-start',
        paddingRight: theme.spacing(5),
      },
      [theme.breakpoints.down('sm')]: {
        display: 'flex',
        flexWrap: 'wrap',
        gap: theme.spacing(1),
        justifyContent: 'flex-start',
        paddingLeft: theme.spacing(2),
        paddingRight: theme.spacing(2),
        minWidth: 0,
      },
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
      },
    },
  };
});


