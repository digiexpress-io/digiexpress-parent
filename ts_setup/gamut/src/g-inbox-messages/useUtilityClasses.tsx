import { Paper, alpha, generateUtilityClass, lighten, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';



export const MUI_NAME = 'GInboxMessages';
export interface GInboxMessagesClasses {
  root: string;
  header: string;
  messages: string;
  newMessage: string;
  attachments: string;
  title: string;

  msgItemRoot: string;
  msgItemSender: string;
  msgItemSentat: string;
  msgItemMyMessage: string;
  msgItemTheirMessage: string;
  msgItemCommentText: string;

  newMsgRoot: string;
  newMsgButtons: string;
  newMsgTitle: string;
  newMsgSenderName: string;
  newMsgAddButton: string;
  newMsgCancelButton: string;

  msgNotAllowedRoot: string;
  msgNotAllowedContent: string;
  msgNotAllowedContentSpacing: string;
  msgNotAllowedContentFlex: string;
  msgNotAllowedIcon: string;
}
export type GInboxMessagesClassKey = keyof GInboxMessagesClasses;


export const GInboxMessagesRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.header,
      styles.messages,
      styles.newMessage,
      styles.attachments,
      styles.title,

      styles.msgItemRoot,
      styles.msgItemSender,
      styles.msgItemSentAt,
      styles.msgItemMyMessage,
      styles.msgItemTheirMessage,
      styles.msgItemCommentText,

      styles.newMsgRoot,
      styles.newMsgButtons,
      styles.newMsgTitle,
      styles.newMsgSenderName,
      styles.newMsgAddButton,
      styles.newMsgCancelButton,

      styles.msgNotAllowedRoot,
      styles.msgNotAllowedContent,
      styles.msgNotAllowedContentSpacing,
      styles.msgNotAllowedContentFlex,
      styles.msgNotAllowedIcon

    ];
  },
})(({ theme }) => {

  return {
    marginTop: theme.spacing(3),

    '& .GInboxMessages-newMessage': {
      padding: theme.spacing(1),
    },
    '& .GInboxMessages-title': {
      margin: theme.spacing(1),
      fontWeight: 'bold'
    },
    '& .GInboxMessages-title .MuiTypography-root': {
      fontWeight: 'bold',
      marginLeft: theme.spacing(2)
    },
    '& .GInboxMessages-addButton': {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
    },
    '& .GInboxMessages-header': {
      [theme.breakpoints.down('lg')]: {
        flexWrap: 'wrap'
      },
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center',
      paddingLeft: theme.spacing(2),
      paddingRight: theme.spacing(2),
      marginTop: theme.spacing(3),
      marginBottom: theme.spacing(5),
    },

    '& .GInboxMessages-messages': {
      margin: theme.spacing(1),
      padding: theme.spacing(1),
    },
    // date positioning
    '& .MuiGrid-item:last-of-type': {
      [theme.breakpoints.up('lg')]: {
        justifyContent: 'flex-start'
      }
    },
    //----- offer name + preview of lastMessage commentText
    '& .MuiGrid-item .MuiBox-root': {
      overflow: "hidden",
      textOverflow: "ellipsis",
      maxWidth: '60rem',
      whiteSpace: 'pre'

    },
    '& .MuiGrid-item .MuiBox-root .MuiTypography-root:first-of-type': {
      fontWeight: 'bold',
      component: 'span'
    },
    // ------

    '.GInboxMessages-msgNotAllowedRoot': {
      paddingLeft: theme.spacing(3),
      paddingRight: theme.spacing(3),
      paddingBottom: theme.spacing(3),
    },

    '.GInboxMessages-msgNotAllowedContentSpacing': {
      padding: theme.spacing(1),
    },

    '.GInboxMessages-msgNotAllowedContent': {
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1)
    },

    '.GInboxMessages-msgNotAllowedIcon': {
      color: theme.palette.primary.main,
      marginRight: theme.spacing(2)
    },

    '.GInboxMessages-msgNotAllowedContentFlex': {
      display: 'flex',
      alignItems: 'center',
      marginBottom: theme.spacing(1)

    },

    '.GInboxMessages-newMsgRoot': {
      display: 'flex',
      flexDirection: 'column',
      width: '100%',
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
      paddingBottom: theme.spacing(1),
      backgroundColor: theme.palette.background.default,
    },

    '& .GInboxMessages-newMsgSenderName': {
      fontWeight: 'bold'
    },

    '& .GInboxMessages-newMsgTitle': {
      display: 'flex',
      flexDirection: 'column',
      padding: theme.spacing(2),
    },
    '& .GInboxMessages-newMsgButtons': {
      display: 'flex',
      flexWrap: 'wrap',
      justifyContent: 'space-between',
    },

    '& .GInboxMessages-newMsgButtons .MuiButtonBase-root': {
      marginTop: theme.spacing(1),
      borderRadius: theme.spacing(4),
      minWidth: '30%',
      [theme.breakpoints.down('sm')]: {
        width: '100%'
      }
    },

    '& .GInboxMessages-newMsgCancelButton.MuiButtonBase-root': {
      color: theme.palette.error.main,
      borderColor: theme.palette.error.main,
      padding: theme.spacing(1),
      ':hover': {
        color: theme.palette.error.main,
        borderColor: theme.palette.error.dark,
        backgroundColor: alpha(theme.palette.error.main, 0.2)
      },
      ':disabled': {
        color: theme.palette.text.disabled,
        borderColor: theme.palette.text.disabled,
        cursor: 'not-allowed'
      },
    },

    '& .GInboxMessages-newMsgAddButton .MuiButton-root': {},

    '.GInboxMessages-msgItemRoot': {
      display: 'flex',
      flexDirection: 'column',
      width: '100%',
      padding: theme.spacing(2),
      margin: theme.spacing(0.5),
      backgroundColor: theme.palette.background.default,
      border: `1px solid ${lighten(theme.palette.action.disabled, 0.7)}`,
    },

    '& .GInboxMessages-msgItemSender': {
      display: 'flex',
      alignItems: 'center',
      marginBottom: theme.spacing(1),
    },

    '& .GInboxMessages-msgItemSentat': {
      display: 'flex',
      justifyContent: 'flex-end'
    },

    '& .GInboxMessages-msgItemCommentText': {

    },

    '& .GInboxMessages-msgItemMyMessage': {
      backgroundColor: theme.palette.info.main,
      marginRight: theme.spacing(1),
    },

    '& .GInboxMessages-msgItemTheirMessage': {
      backgroundColor: theme.palette.success.main,
      marginRight: theme.spacing(1),
    },

    '& .GInboxMessages-msgItemCommentText .MuiTypography-root': {
      fontSize: theme.typography.body2.fontSize,
    },

    '& .GInboxMessages-msgItemSentat .MuiTypography-root': {
      fontSize: theme.typography.caption.fontSize,
    },

    '& .GInboxMessages-msgItemSender .MuiTypography-root': {
      fontWeight: 'bold',
      fontSize: theme.typography.body2.fontSize,
    }
  };
});


export const GInboxMessageRoot = styled('div', {
  name: MUI_NAME,
  slot: 'MessageItem',
  overridesResolver: (_props, styles) => {
    return [
      styles.msgItemRoot,

    ];
  },
})(({ theme }) => {
  return {

  };
});



export const GInboxNewMessageRoot = styled(Paper, {
  name: MUI_NAME,
  slot: 'NewMessage',
  overridesResolver: (_props, styles) => {
    return [
      styles.newMsgRoot
    ];
  },
})(({ theme }) => {
  return {

  };
});

export const GInboxMessageNotAllowedRoot = styled(Paper, {
  name: MUI_NAME,
  slot: 'MessageNotAllowed',
  overridesResolver: (_props, styles) => {
    return [
      styles.msgNotAllowedRoot,
      styles.msgNotAllowedContent,
      styles.msgNotAllowedContentSpacing,
      styles.msgNotAllowedContentFlex,
      styles.msgNotAllowedIcon
    ];
  },
})(({ theme }) => {
  return {

  };
});


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    header: ['header'],
    messages: ['messages'],
    newMessage: ['newMessage'],
    attachments: ['attachments'],
    title: ['title'],

    msgItemRoot: ['msgItemRoot'],
    msgItemSender: ['msgItemSender'],
    msgItemSentat: ['msgItemSentat'],
    msgItemMyMessage: ['msgItemMyMessage'],
    msgItemTheirMessage: ['msgItemTheirMessage'],
    msgItemCommentText: ['msgItemCommentText'],

    newMsgRoot: ['newMsgRoot'],
    newMsgButtons: ['newMsgButtons'],
    newMsgTitle: ['newMsgTitle'],
    newMsgSenderName: ['newMsgSenderName'],
    newMsgAddButton: ['newMsgAddButton'],
    newMsgCancelButton: ['newMsgCancelButton'],

    msgNotAllowedRoot: ['msgNotAllowedRoot'],
    msgNotAllowedContent: ['msgNotAllowedContent'],
    msgNotAllowedContentSpacing: ['msgNotAllowedContentSpacing'],
    msgNotAllowedContentFlex: ['msgNotAllowedContentFlex'],
    msgNotAllowedIcon: ['msgNotAllowedIcon']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
