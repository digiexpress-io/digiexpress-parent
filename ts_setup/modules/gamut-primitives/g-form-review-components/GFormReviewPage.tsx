import React from 'react';
import { Typography, Grid2, styled, generateUtilityClass, useThemeProps } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

import { GFormReviewContext } from './GFormReviewContext';
import { GOverridableComponent } from '../g-override';



const MUI_NAME = 'GFormReviewPage';

export interface GFormReviewPageClasses {
  root: string,
  groupLabel: string
}

export type GFormReviewPageClassKey = keyof GFormReviewPageClasses;


export interface PageItemProps {
  item: {
    label: Record<string, string>;
    items?: string[];
  };
  component?: GOverridableComponent<PageItemProps>;
  className?: string | undefined;
}
export const GFormReviewPage: React.FC<PageItemProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);;


  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);


  const items = props.item.items ? props.item.items.map(id => dC.createItem(id)) : null;
  const label = dC.getTranslated(props.item.label);
  const Root = props.component ?? GFormReviewPageRoot;
  return (
    <Root className={classes.root} ownerState={props}>
      <Grid2 size={{ xs: 2 }}>
        <Typography className={classes.groupLabel}>{label}</Typography>
      </Grid2>
      {items?.map((i, k) => <Grid2 key={k} size={{ xs: 12 }}>{i}</Grid2>)}

    </Root>
  );
}



const useUtilityClasses = (ownerState: PageItemProps) => {

  const slots = {
    root: ['root'],
    groupLabel: ['groupLabel']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


const GFormReviewPageRoot = styled(Grid2, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.groupLabel
    ];
  },
})(({ theme }) => {
  return {

    '.GFormReviewPage-groupLabel': {
      ...theme.typography.h2
    }
  };
});
