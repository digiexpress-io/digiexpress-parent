import React, { useContext } from 'react';
import { Grid } from '@mui/material';
import { GFormReviewContext } from './GFormReviewContext';
import type { ItemProps } from './componentTypes';

interface QuestionnaireItemProps extends ItemProps {
  title: string;
  item: {
    items?: string[];
  };
}

export const GFormReviewQuestionnaire: React.FC<QuestionnaireItemProps> = ({ item }) => {
  const dC = useContext(GFormReviewContext);;
  const items = item.items ? item.items.map(id => dC.createItem(id, null, true)) : null;
  return (
    <Grid container spacing={1}>
      {items}
    </Grid>
  );
}
