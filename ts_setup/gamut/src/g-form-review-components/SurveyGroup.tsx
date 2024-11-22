import React, { useContext } from 'react';
import { Typography, Paper, Grid, Box, styled } from '@mui/material';
import Markdown from 'react-markdown';

import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GroupContext } from './GFormReviewGroupContext';
import { GFormReviewSurvey } from './Survey';


const PaddedPaper = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(2)
}));

const SurveyContainer = styled(Box)({
  display: 'grid',
  alignItems: 'center',
  width: '100%'
});

const SurveyContainerVertical = styled(SurveyContainer)({
  gridAutoFlow: 'column'
});

const SurveyContainerHorizontal = styled(SurveyContainer)({
  gridAutoFlow: 'row'
});

const SurveyHeader = styled(Box)(({ theme }) => ({
  fontWeight: '450',
  padding: theme.spacing(1)
}));

type Variant = 'h3' | 'h4' | 'h5' | 'h6';

const groupLevels: Record<number, Variant> = {
  1: 'h3',
  2: 'h4',
  3: 'h5',
  4: 'h6'
};

export const GFormReviewSurveyGroup: React.FC<ItemProps> = ({ item }) => {
  const dC = useContext(GFormReviewContext);;
  const groupCtx = useContext(GroupContext);

  const surveys: string[] = [];
  const items: string[] = [];

  if (item.items) {
    for (const itemId of item.items) {
      const item: { type: string | any } = dC.getItem(itemId) as any;

      if (!item) continue;
      if (item.type === 'survey') {
        surveys.push(itemId);
      } else {
        items.push(itemId);
      }
    }
  }

  const valueSet = dC.findValueSet(item.valueSetId);

  const vertical = item.view === 'verticalSurveygroup';
  const optionCount = valueSet?.entries.length || 0;

  const rowCount = vertical ? optionCount + 1 : surveys.length;
  const colCount = vertical ? surveys.length : optionCount;

  const normalItems = items.map(id => dC.createItem(id)).filter(item => item);

  const description = dC.getTranslated(item.description);

  const fontWeight = 550 - groupCtx.level * 50;

  const surveyContainerStyle: React.CSSProperties = {
    gridTemplateRows: `repeat(${rowCount}, auto)`,
    gridTemplateColumns: `30% repeat(${colCount}, fit-content(30%))`
  };

  const children = (
    <>
      <Box></Box>
      {
        valueSet && valueSet.entries.map((entry) => (
          <SurveyHeader key={entry.id}>{dC.getTranslated(entry.label)}</SurveyHeader>
        ))
      }
      {
        valueSet && surveys.map((itemId, n) => (
          <GFormReviewSurvey key={itemId} id={itemId} valueSet={valueSet} even={n % 2 === 0} />
        ))
      }
    </>
  );

  return (
    <GroupContext.Provider value={{ level: groupCtx.level < 4 ? groupCtx.level + 1 : groupCtx.level }}>
      <PaddedPaper data-type='group-paper' elevation={groupCtx.level}>
        <Grid data-type='group-grid' container spacing={2}>
          <Grid data-type='group-title' item xs={12}>
            <Typography variant={groupLevels[groupCtx.level]} fontWeight={fontWeight}>
              {dC.getTranslated(item.label)}
            </Typography>
          </Grid>

          {
            description && <Markdown children={description} skipHtml />
          }

          <Grid item xs={12}>
            {vertical ?
              <SurveyContainerVertical style={surveyContainerStyle}
              >
                {children}
              </SurveyContainerVertical> :
              <SurveyContainerHorizontal style={surveyContainerStyle}>
                {children}
              </SurveyContainerHorizontal>
            }
          </Grid>

          {normalItems.map((i, k) => <Grid data-type='group-item-grid' key={k} item xs={12}>{i}</Grid>)}

        </Grid>
      </PaddedPaper>
    </GroupContext.Provider>
  );
}
