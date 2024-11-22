import React, { useContext } from 'react';
import { Typography, Paper, Grid, styled } from '@mui/material';

import { GFormReviewContext } from './GFormReviewContext';
import { GroupContext } from './GFormReviewGroupContext';

import Markdown from 'react-markdown';

const PaddedPaper = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(2)
}));

type Variant = 'h3' | 'h4' | 'h5' | 'h6';

const groupLevels: Record<number, Variant> = {
  1: 'h3',
  2: 'h4',
  3: 'h5',
  4: 'h6'
};

export type GroupItemProps = {
  item: {
    label: Record<string, string>;
    items?: string[];
    description?: Record<string, string>
  };

}

export const GFormReviewGroup: React.FC<GroupItemProps> = ({ item }) => {
  const dC = useContext(GFormReviewContext);;
  const groupCtx = useContext(GroupContext);
  const items = item.items ? item.items.map(id => dC.createItem(id)).filter(item => item) : null;
  if (!items || items.length === 0) {
    return null;
  }

  const description: string = dC.getTranslated(item.description);
  const groupLevel: number = groupCtx.level;
  const variant: Variant = groupLevels[groupLevel];
  const fontWeight: number = 500 - groupLevel * 50;
  return (
    <GroupContext.Provider value={{ level: groupCtx.level < 4 ? groupCtx.level + 1 : groupCtx.level }}>
      <PaddedPaper data-type='group-paper' elevation={groupCtx.level}>
        <Grid data-type='group-grid' container spacing={2}>
          <Grid data-type='group-title' item xs={12}>
            <Typography variant={variant} fontWeight={fontWeight}>
              {dC.getTranslated(item.label)}
            </Typography>
          </Grid>

          {
            description && <Markdown children={description} skipHtml />
          }

          {items.map((i, key) => <Grid data-type='group-item-grid' key={key} item xs={12}>{i}</Grid>)}
        </Grid>
      </PaddedPaper>
    </GroupContext.Provider>
  );
}
