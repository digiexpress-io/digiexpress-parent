import { alpha, Dialog, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GArticleFeedbackViewer';

export interface GArticleFeedbackViewerClasses {
  root: string;
  titleContainer: string;
  title: string;
  loginReqPopoverMsgContainer: string;
  voteTitle: string;
  thumbsContainer: string;
  iconSize: string;
  contentDivider: string;
  subTitle: string;
  replyContainer: string;
  answerSubTitle: string;
  titleDateContainer: string;
}

export type GArticleFeedbackViewerClassKey = keyof GArticleFeedbackViewerClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    titleContainer: ['titleContainer'],
    title: ['title'],
    loginReqPopoverMsgContainer: ['loginReqPopoverMsgContainer'],
    voteTitle: ['voteTitle'],
    thumbsContainer: ['thumbsContainer'],
    iconSize: ['iconSize'],
    contentDivider: ['contentDivider'],
    subTitle: ['subTitle'],
    replyContainer: ['replyContainer'],
    answerSubTitle: ['answerSubTitle'],
    titleDateContainer: ['titleDateContainer'],
  };

  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GArticleFeedbackViewerRoot = styled(Dialog, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.titleContainer,
      styles.title,
      styles.loginReqPopoverMsgContainer,
      styles.voteTitle,
      styles.thumbsContainer,
      styles.iconSize,
      styles.contentDivider,
      styles.subTitle,
      styles.replyContainer,
      styles.answerSubTitle,
      styles.titleDateContainer,
    ];
  },
})(({ theme }) => {

  return {

    '& .GArticleFeedbackViewer-titleContainer': {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      gap: theme.spacing(1.5),
      position: 'relative',
    
      [theme.breakpoints.down('sm')]: {
        alignItems: 'flex-start',
      },
    },      
    '& .GArticleFeedbackViewer-loginReqPopoverMsgContainer': {
      display: 'flex',
      alignItems: 'center',
    },
    '& .GArticleFeedbackViewer-voteTitle': {
      textAlign: 'right',
      alignSelf: 'flex-end',
    
      [theme.breakpoints.down('sm')]: {
        textAlign: 'right',
        alignSelf: 'flex-start',
      },
    },    
    '& .GArticleFeedbackViewer-thumbsContainer': {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'flex-end',
    
      [theme.breakpoints.down('sm')]: {
        marginTop: theme.spacing(1),
      },
    },       
    '& .GArticleFeedbackViewer-iconSize': {
      fontSize: '30pt',
    
      [theme.breakpoints.down('sm')]: {
        fontSize: '20pt',
      },
    },    
    '& .GArticleFeedbackViewer-contentDivider': {
      marginTop: theme.spacing(4),
      border: `2px solid ${theme.palette.primary.main}`,
    },
    '& .GArticleFeedbackViewer-title': {
      ...theme.typography.h1,
      marginBottom: theme.spacing(2),
    },
    '& .GArticleFeedbackViewer-subTitle': {
      ...theme.typography.body1,
      fontWeight: 'bold',
      marginBottom: theme.spacing(2),
    },    
    '& .GArticleFeedbackViewer-replyContainer': {
      backgroundColor: alpha(theme.palette.primary.main, 0.1),
      padding: theme.spacing(2),
    },
    '& .GArticleFeedbackViewer-answerSubTitle': {
      ...theme.typography.body1,
      fontWeight: 'bold',
      marginBottom: theme.spacing(2),
    },
    '& .GArticleFeedbackViewer-titleDateContainer': {
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1.5),
    
      [theme.breakpoints.down('sm')]: {
        flexDirection: 'column',
        alignItems: 'flex-start',
      },
    },         

  }
});