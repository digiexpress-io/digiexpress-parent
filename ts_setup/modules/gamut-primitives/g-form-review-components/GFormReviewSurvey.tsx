import React from 'react';
import { Box, generateUtilityClass, styled, useThemeProps } from '@mui/material';
import { RadioButtonChecked, RadioButtonUnchecked } from '@mui/icons-material';
import composeClasses from '@mui/utils/composeClasses';

import { GFormReviewContext } from './GFormReviewContext';
import { GOverridableComponent } from '../g-override';


const MUI_NAME = 'GFormReviewSurvey';

export interface GFormReviewSurveyClasses {
  root: string,
  questionLabel: string,
  questionItem: string,
  questionItemEven: string
}

export type GFormReviewSurveyClassKey = keyof GFormReviewSurveyClasses;


export interface SurveyProps {
  id: string;
  valueSet: any;
  even: boolean;

  component?: GOverridableComponent<SurveyProps>;
  className?: string | undefined;
};


export const GFormReviewSurvey: React.FC<SurveyProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);

  const survey: { label: Record<string, string> | any } = dC.getItem(props.id) as any
  const answer = dC.getAnswer(props.id);

  const Root = props.component ?? GFormReviewSurveyRoot;
  return (
    <Root className={classes.root} ownerState={props}>
      <Box className={classes.questionLabel}>{dC.getTranslated(survey.label)}</Box>

      {props.valueSet ? (props.valueSet.entries.map((entry: SurveyProps) => {
        const children = entry.id === answer ? <RadioButtonChecked /> : <RadioButtonUnchecked />;
        if (props.even) {
          return <Box className={classes.questionItemEven} key={entry.id}>{children}</Box>
        } else {
          return <Box className={classes.questionItem} key={entry.id}>{children}</Box>
        }
      })
      ) : (
        undefined
      )}

    </Root>
  );
}


const useUtilityClasses = (ownerState: SurveyProps) => {
  const slots = {
    root: ['root', ownerState.id],
    questionLabel: ['questionLabel'],
    questionItem: ['questionItem'],
    questionItemEven: ['questionItemEven'],

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


const GFormReviewSurveyRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.questionLabel,
      styles.questionItem,
      styles.questionItemEven
    ];
  },
})(({ theme }) => {
  return {

    '.GFormReviewSurvey-questionLabel': {
      fontWeight: 'bold',
      padding: theme.spacing(1),
    },
    '.GFormReviewSurvey-questionItem': {
      textAlign: 'center',
      padding: theme.spacing(1)
    },
    '.GFormReviewSurvey-questionItemEven': {
      textAlign: 'center',
      padding: theme.spacing(1),
      backgroundColor: theme.palette.background.default
    }
  };
});


