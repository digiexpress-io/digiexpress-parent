import { alpha, Dialog, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GArticleFeedbackViewer';

export interface GArticleFeedbackViewerClasses {
  root: string;
  titleContainer: string;
  title: string;
  loginReqPopoverMsgContainer: string;
  thumbsContainer: string;
  iconSize: string;
  contentDivider: string;
  subTitle: string;
  replyContainer: string;
  answerSubTitle: string;
}

export type GArticleFeedbackViewerClassKey = keyof GArticleFeedbackViewerClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    titleContainer: ['titleContainer'],
    title: ['title'],
    loginReqPopoverMsgContainer: ['loginReqPopoverMsgContainer'],
    thumbsContainer: ['thumbsContainer'],
    iconSize: ['iconSize'],
    contentDivider: ['contentDivider'],
    subTitle: ['subTitle'],
    replyContainer: ['replyContainer'],
    answerSubTitle: ['answerSubTitle'],
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
      styles.thumbsContainer,
      styles.iconSize,
      styles.contentDivider,
      styles.subTitle,
      styles.replyContainer,
      styles.answerSubTitle,
    ];
  },
})(({ theme }) => {

  return {

    '& .GArticleFeedbackViewer-titleContainer': {
      display: 'flex'
    },
    '& .GArticleFeedbackViewer-loginReqPopoverMsgContainer': {
      display: 'flex',
      alignItems: 'center',
    },
    '& .GArticleFeedbackViewer-thumbsContainer': {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'end'
    },
    '& .GArticleFeedbackViewer-iconSize': {
      fontSize: '30pt'
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

  }
});