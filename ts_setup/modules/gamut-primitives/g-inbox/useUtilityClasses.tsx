import { alpha, generateUtilityClass, styled } from '@mui/material';
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
  headerLastModified: string,
  files: string;
  filesCount: string;
  noValue: string;
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
    headerLastModified: ['headerLastModified'],
    files: ['files'],
    filesCount: ['filesCount'],
    noValue: ['noValue'],
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
      styles.headerLastModified,
      styles.files,
      styles.filesCount,
      styles.noValue,
    ];
  },
})(({ theme }) => {
  const color = theme.palette.info.main;
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
        backgroundColor: alpha(theme.palette.action.active, 0.05),
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
        flexBasis: '50%',
        maxWidth: '50%',
        display: 'flex',
        justifyContent: 'flex-end',
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
        flexBasis: '50%',
        maxWidth: '50%',
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
    },

    '.GInbox-headerLastModified': {
      display: 'flex',
      justifyContent: 'flex-end',
    },

    '& .GInbox-files': {
      marginRight: theme.spacing(0.5),
    },
    '& .GInbox-filesCount': {
      marginTop: '4px',
      marginBottom: '4px',
      height: '28px',
      width: '28px',
      backgroundColor: alpha(color, 0.3),
      color: theme.palette.text.primary,
      [theme.breakpoints.down('md')]: {
        height: '24px',
        width: '24px',
      },
    },    
    '& .GInbox-noValue': {
      height: '28px',
      width: '28px',
      backgroundColor: 'unset',
      color: theme.palette.text.primary,
      [theme.breakpoints.down('md')]: {
        height: '24px',
        width: '24px',
      },
    },    

  };
});


