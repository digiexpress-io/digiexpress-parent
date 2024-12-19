import React from 'react';
import { Typography, Paper, Grid2, Box, styled, useThemeProps, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import Markdown from 'react-markdown';

import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GroupContext } from './GFormReviewGroupContext';
import { GFormReviewSurvey } from './GFormReviewSurvey';



const MUI_NAME = 'GFormReviewSurveyGroup';

export interface GFormReviewSurveyGroupClasses {
  root: string;
}

export type GFormReviewSurveyGroupClassKey = keyof GFormReviewSurveyGroupClasses;



export const GFormReviewSurveyGroup: React.FC<ItemProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);


  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);


  const groupCtx = React.useContext(GroupContext);

  const surveys: string[] = [];
  const items: string[] = [];

  if (props.item.items) {
    for (const itemId of props.item.items) {
      const item: { type: string | any } = dC.getItem(itemId) as any;

      if (!item) continue;
      if (item.type === 'survey') {
        surveys.push(itemId);
      } else {
        items.push(itemId);
      }
    }
  }

  const valueSet = dC.findValueSet(props.item.valueSetId);

  const vertical = props.item.view === 'verticalSurveygroup';
  const optionCount = valueSet?.entries.length || 0;

  const rowCount = vertical ? optionCount + 1 : surveys.length;
  const colCount = vertical ? surveys.length : optionCount;

  const normalItems = items.map(id => dC.createItem(id)).filter(item => item);

  const description = dC.getTranslated(props.item.description);

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
  const Root = props.component ?? GFormReviewSurveyGroupRoot;

  return (
    <GroupContext.Provider value={{ level: groupCtx.level < 4 ? groupCtx.level + 1 : groupCtx.level }}>
      <Root className={classes.root} ownerState={props}>
        <Paper data-type='group-paper' elevation={groupCtx.level}>
          <Grid2 data-type='group-grid'>
            <Grid2 data-type='group-title' size={{ xs: 12 }}>
              <Typography variant={groupLevels[groupCtx.level]} fontWeight={fontWeight}>
                {dC.getTranslated(props.item.label)}
              </Typography>
            </Grid2>

            {
              description && <Markdown children={description} skipHtml />
            }

            <Grid2 size={{ xs: 12 }}>
              {vertical ?
                <SurveyContainerVertical style={surveyContainerStyle}
                >
                  {children}
                </SurveyContainerVertical> :
                <SurveyContainerHorizontal style={surveyContainerStyle}>
                  {children}
                </SurveyContainerHorizontal>
              }
            </Grid2>

            {normalItems.map((i, k) => <Grid2 data-type='group-item-grid' key={k} size={{ xs: 12 }}>{i}</Grid2>)}

          </Grid2>
        </Paper>
      </Root>
    </GroupContext.Provider>
  );
}


const useUtilityClasses = (ownerState: ItemProps) => {
  const slots = {
    root: ['root', ownerState.item.id],

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


const GFormReviewSurveyGroupRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {

  };
});



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