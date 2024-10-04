import { Paper, alpha, generateUtilityClass, lighten, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';



export const MUI_NAME = 'GInboxSubject';
export interface GInboxSubjectClasses {
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
}
export type GInboxSubjectClassKey = keyof GInboxSubjectClasses;


export const GInboxSubjectRoot = styled("div", {
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
    ];
  },
})(({ theme }) => {

  return {
    marginTop: theme.spacing(3),

    //GInboxSubject
    '& .GInboxSubject-newMessage': {
      padding: theme.spacing(1),
    },
    '& .GInboxSubject-title': {
      margin: theme.spacing(1),
      fontWeight: 'bold'
    },
    '& .GInboxSubject-title .MuiTypography-root': {
      fontWeight: 'bold'
    },
    '& .GInboxSubject-addButton': {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
    },

    '& .GInboxSubject-header': {
      paddingLeft: theme.spacing(2),
      paddingRight: theme.spacing(2),
      marginTop: theme.spacing(3),
      marginBottom: theme.spacing(5),
    },

    '& .GInboxSubject-attachments': {
      display: 'flex',
      flexDirection: 'row',
      flexWrap: 'wrap',
      alignItems: 'center',
    },

    '& .GInboxSubject-messages': {
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

    '.GInboxSubject-newMsgRoot': {
      display: 'flex',
      flexDirection: 'column',
      width: '100%',
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
      paddingBottom: theme.spacing(1),
      backgroundColor: theme.palette.background.default,
    },

    '& .GInboxSubject-newMsgTitle .MuiTypography-root': {
      fontSize: theme.typography.caption.fontSize
    },

    '& .GInboxSubject-newMsgSenderName': {
      fontWeight: 'bold'
    },

    '& .GInboxSubject-newMsgTitle': {
      display: 'flex',
      flexDirection: 'column',
      padding: theme.spacing(2),

    },
    '& .GInboxSubject-newMsgButtons': {
      display: 'flex',
      flexWrap: 'wrap',
      justifyContent: 'space-between',
    },

    '& .GInboxSubject-newMsgButtons .MuiButtonBase-root': {
      marginTop: theme.spacing(1),
      borderRadius: theme.spacing(4),
      minWidth: '30%',
      [theme.breakpoints.down('sm')]: {
        width: '100%'
      }
    },

    '& .GInboxSubject-newMsgCancelButton.MuiButtonBase-root': {
      color: theme.palette.error.main,
      borderColor: theme.palette.error.main,
      padding: theme.spacing(1),
      ':hover': {
        color: theme.palette.error.main,
        borderColor: theme.palette.error.dark,
        backgroundColor: alpha(theme.palette.error.main, 0.2)
      }
    },
    '& .GInboxSubject-newMsgAddButton .MuiButton-root': {},

    '.GInboxSubject-msgItemRoot': {
      display: 'flex',
      flexDirection: 'column',
      width: '100%',
      padding: theme.spacing(2),
      margin: theme.spacing(0.5),
      backgroundColor: theme.palette.background.default,
      border: `1px solid ${lighten(theme.palette.action.disabled, 0.7)}`,
    },

    '& .GInboxSubject-msgItemSender': {
      display: 'flex',
      alignItems: 'center',
      marginBottom: theme.spacing(1),
    },

    '& .GInboxSubject-msgItemSentat': {
      display: 'flex',
      justifyContent: 'flex-end'
    },

    '& .GInboxSubject-msgItemCommentText': {

    },

    '& .GInboxSubject-msgItemMyMessage': {
      backgroundColor: theme.palette.info.main,
      marginRight: theme.spacing(1),
    },

    '& .GInboxSubject-msgItemTheirMessage': {
      backgroundColor: theme.palette.success.main,
      marginRight: theme.spacing(1),
    },

    '& .GInboxSubject-msgItemCommentText .MuiTypography-root': {
      fontSize: theme.typography.body2.fontSize,
    },

    '& .GInboxSubject-msgItemSentat .MuiTypography-root': {
      fontSize: theme.typography.caption.fontSize,
    },

    '& .GInboxSubject-msgItemSender .MuiTypography-root': {
      fontWeight: 'bold',
      fontSize: theme.typography.body2.fontSize,
    }
  };
});


export const GInboxSubjectMessageRoot = styled('div', {
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



export const GInboxSubjectNewMessageRoot = styled(Paper, {
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
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
