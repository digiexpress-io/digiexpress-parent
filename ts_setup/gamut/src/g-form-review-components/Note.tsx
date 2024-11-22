import React, { useContext } from 'react';
import { Paper, styled } from '@mui/material';
import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';

import { GMarkdown } from '../g-md';

const PaddedPaper = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(2)
}));

export const GFormReviewNote: React.FC<ItemProps> = ({ item }) => {
  const dC = useContext(GFormReviewContext);;
  const label = dC.getTranslated(item.label);
  return (
    <PaddedPaper variant='outlined'>
      <GMarkdown children={dC.substituteVariables(label)} />
    </PaddedPaper>
  );
}
