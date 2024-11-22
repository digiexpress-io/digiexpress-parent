import React, { useContext } from 'react';
import { Box, styled } from '@mui/material';
import { RadioButtonChecked, RadioButtonUnchecked } from '@mui/icons-material';

import { GFormReviewContext } from './GFormReviewContext';

const QuestionLabel = styled(Box)(({ theme }) => ({
  fontWeight: 'bold',
  padding: theme.spacing(1),
}));

const QuestionItem = styled(Box)(({ theme }) => ({
  textAlign: 'center',
  padding: theme.spacing(1)
}));

const QuestionItemEven = styled(QuestionItem)(({ theme }) => ({
  backgroundColor: theme.palette.background.default
}));

export interface SurveyProps {
  id: string;
  valueSet: any;
  even: boolean;
};

const ValueSet: React.FC<{ valueSet: any, answer: any, even: boolean }> = ({ valueSet, answer, even }) => {
  if (!valueSet) {
    return null;
  }
  return valueSet.entries.map((entry: SurveyProps) => {
    const children = entry.id === answer ? <RadioButtonChecked /> : <RadioButtonUnchecked />;
    if (even) {
      return <QuestionItemEven key={entry.id}>{children}</QuestionItemEven>
    } else {
      return <QuestionItem key={entry.id}>{children}</QuestionItem>
    }
  });
}

export const GFormReviewSurvey: React.FC<SurveyProps> = ({ id, valueSet, even }) => {
  const dC = useContext(GFormReviewContext);;
  const survey: { label: Record<string, string> | any } = dC.getItem(id) as any
  const answer = dC.getAnswer(id);

  return (
    <>
      <QuestionLabel>{dC.getTranslated(survey.label)}</QuestionLabel>
      <ValueSet valueSet={valueSet} answer={answer} even={even} />
    </>
  );
}
