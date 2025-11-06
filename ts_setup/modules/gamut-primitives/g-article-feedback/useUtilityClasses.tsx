import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '@dxs-ts/gamut-api';
import { GArticleFeedbackProps } from './GArticleFeedback';
import { GFeedbackTableArticleReducerProps } from './GArticleFeedbackTableReducer';


export const MUI_NAME = 'GArticleFeedback';

export interface GArticleFeedbackClasses {
  root: string;
}

export type GArticleFeedbackClassKey = keyof GArticleFeedbackClasses;


export const useUtilityClasses = (ownerState: GArticleFeedbackProps) => {
  const slots = {
    root: ['root'],
    emptyRow: ['emptyRow'],
    filledRow: ['filledRow'],
    pagination: ['pagination'],
    noData: ['noData'],
    toolbar: ['toolbar'],
    vote: ['vote'],
    colWidth: ['colWidth']
  };

  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GArticleFeedbackRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {

    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GArticleFeedbackProps & { reducer: GFeedbackTableArticleReducerProps } }>(({ theme, ownerState }) => {
  const enabled = (ownerState.enabled && ownerState.children && ownerState.enabled(ownerState.children)) ?? false;
  const { reducer } = ownerState;


  return {
    display: enabled ? undefined : 'none',

    '& .MuiTableContainer-root': {
      paddingLeft: theme.spacing(5),
      paddingRight: theme.spacing(5),
    },

    '& .GArticleFeedback-emptyRow': {
      height: 33 * reducer[0].emptyRows
    },
    '& .GArticleFeedback-colWidth': {
      [theme.breakpoints.up('sm')]: {
        width: '35%'
      },

    },
    '& .GArticleFeedback-pagination .MuiToolbar-root': {
      paddingLeft: 0
    },
    '& .GArticleFeedback-pagination .MuiInputBase-root': {
      width: "unset"
    },
    '& .GArticleFeedback-filledRow': {
      cursor: 'pointer'
    },
    '& .GArticleFeedback-noData': {
      'textAlign': 'center'
    },
    '& .GArticleFeedback-toolbar': {
      paddingLeft: 0
    },
    '& .GArticleFeedback-toolbar .MuiTypography-root': {
      flex: '1 1 100%',
      ...theme.typography.h3
    },
    '& .GArticleFeedback-vote .MuiTypography-root': {
      ...theme.typography.body2
    },
    '& .GArticleFeedback-vote .MuiSvgIcon-root': {
      fontSize: 'medium'
    },
    '& .GArticleFeedback-vote': {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'flex-end',
      gap: theme.spacing(1),
      '& .vote-item': {
        display: 'flex',
        alignItems: 'center',
        gap: theme.spacing(0.25),
        minWidth: 40,
        justifyContent: 'center'
      },
      '& .vote-count': {
        display: 'inline-block',
        minWidth: 20,
        textAlign: 'center',
        lineHeight: 1,
      }
    },
  };
});