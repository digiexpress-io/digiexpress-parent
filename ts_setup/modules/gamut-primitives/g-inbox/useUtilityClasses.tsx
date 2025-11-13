import { generateUtilityClass, lighten, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GInbox';

export interface GInboxClasses {
  root: string,
  inboxItem: string,
  inboxItemTitle: string,
  inboxItemSentAt: string,
  inboxItemAttachments: string,
  taskRefLayout: string,
  newMsgIndicator: string,
  headerRow: string,
  headerFormName: string,
  headerAttachments: string,
  headerLastModified: string
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
    newMsgIndicator: ['newMsgIndicator'],
    headerRow: ['headerRow'],
    headerFormName: ['headerFormName'],
    headerAttachments: ['headerAttachments'],
    headerLastModified: ['headerLastModified']

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
      styles.newMsgIndicator,
      styles.headerRow,
      styles.headerFormName,
      styles.headerAttachments,
      styles.headerLastModified
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
      backgroundColor: theme.palette.background.default,
      borderWidth: '1px',
      borderBottomStyle: 'solid',
      borderBottomColor: theme.palette.divider,
      ':hover': {
        backgroundColor: theme.palette.action.active,
        borderColor: theme.palette.divider,
      },
    
      [theme.breakpoints.only('sm')]: {
        alignItems: 'flex-start',
      },
    
      [theme.breakpoints.only('md')]: {
        alignItems: 'flex-start',
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
        textAlign: 'left',
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
        display: 'flex',
        alignItems: 'center',
      },
    },

    '.GInbox-inboxItemAttachments': {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'flex-start',
      flexGrow: 1,
      flexShrink: 1,
      minWidth: 0,
      overflow: 'hidden',
      gap: theme.spacing(1),
      '& > *': { flex: '0 0 auto' },
    
      [theme.breakpoints.only('sm')]: {
        flexWrap: 'wrap',
        rowGap: theme.spacing(1),
      },
    
      [theme.breakpoints.only('md')]: {
        flexWrap: 'wrap',
        rowGap: theme.spacing(1),
      },
    
      [theme.breakpoints.down('sm')]: {
        flexWrap: 'wrap',
        paddingLeft: theme.spacing(2),
        paddingRight: theme.spacing(2),
      },
    },    

    '.GInbox-headerRow': {
      width: '100%',
    },

    '.GInbox-headerFormName': {
      display: 'flex',
      justifyContent: 'flex-start !important',
    },

    '.GInbox-headerAttachments': {
      display: 'flex',
      justifyContent: 'flex-start',
      paddingLeft: theme.spacing(1),
    },

    '.GInbox-headerLastModified': {
      display: 'flex',
      justifyContent: 'flex-end',
    },

  };
});


