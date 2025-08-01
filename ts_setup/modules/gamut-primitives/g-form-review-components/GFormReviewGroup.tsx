import React from 'react';
import { Typography, Grid2, styled, generateUtilityClass, useThemeProps, Box, alpha } from '@mui/material';
import Markdown from 'react-markdown';
import composeClasses from '@mui/utils/composeClasses';

import { GFormReviewContext } from './GFormReviewContext';
import { GroupContext } from './GFormReviewGroupContext';
import { GOverridableComponent } from '../g-override';


const MUI_NAME = 'GFormReviewGroup';

export interface GFormReviewGroupClasses {
  root: string,
  border: string
}

export type GFormReviewGroupClassKey = keyof GFormReviewGroupClasses;

export type GroupItemProps = {
  item: {
    label: Record<string, string>;
    items?: string[];
    description?: Record<string, string>
  };

  component?: GOverridableComponent<GroupItemProps>;
  className?: string | undefined;
}

export const GFormReviewGroup: React.FC<GroupItemProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);

  const groupCtx = React.useContext(GroupContext);
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);

  const items = props.item.items ? props.item.items.map(id => dC.createItem(id)).filter(item => item) : null;
  if (!items || items.length === 0) {
    return null;
  }

  const description: string = dC.getTranslated(props.item.description);
  const groupLevel: number = groupCtx.level;
  const variant: Variant = groupLevels[groupLevel];
  const fontWeight: number = 500 - groupLevel * 50;

  const Root = props.component ?? GFormReviewGroupRoot;
  return (
    <GroupContext.Provider value={{ level: groupCtx.level < 4 ? groupCtx.level + 1 : groupCtx.level }}>
      <Root className={classes.root} ownerState={props}>
        <Box data-type='group-paper' className={classes.border}>
          <Grid2 data-type='group-grid'>
            <Grid2 data-type='group-title' size={{ xs: 'auto' }}>
              <Typography variant={variant} fontWeight={fontWeight}>
                {dC.getTranslated(props.item.label)}
              </Typography>
            </Grid2>

            {
              description && <Markdown children={description} skipHtml />
            }

            {items.map((i, key) => <Grid2 data-type='group-item-grid' key={key} size={{ xs: 'auto' }}> {i}</Grid2>)}
          </Grid2>
        </Box>
      </Root>
    </GroupContext.Provider >
  );
}



const useUtilityClasses = (ownerState: GroupItemProps) => {
  const slots = {
    root: ['root'],
    border: ['border']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


const GFormReviewGroupRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.border
    ];
  },
})(({ theme }) => {
  return {

    '.GFormReviewGroup-border': {
      borderLeft: `2px solid ${alpha(theme.palette.primary.main, 0.5)}`,
      borderBottom: `2px solid ${alpha(theme.palette.primary.main, 0.5)}`,
      padding: theme.spacing(2),
      marginTop: theme.spacing(0.5),
      marginBottom: theme.spacing(0.5)
    }
  };
});



type Variant = 'h3' | 'h4' | 'h5' | 'h6';

const groupLevels: Record<number, Variant> = {
  1: 'h3',
  2: 'h4',
  3: 'h5',
  4: 'h6'
};

