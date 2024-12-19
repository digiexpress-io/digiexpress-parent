import React from 'react';
import { generateUtilityClass, Grid2, styled, Typography, useThemeProps } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

import { RowGroupContext } from './GFormReviewRowGroupContext';
import { GOverridableComponent } from '../g-override';


const MUI_NAME = 'GFormReviewItem';

export interface GFormReviewItemClasses {
  root: string;
  label: string;
  body: string;
}

export type GFormReviewItemClassKey = keyof GFormReviewItemClasses;


interface ItemProps {
  label: string;
  children: JSX.Element;
  component?: GOverridableComponent<ItemProps>;
  className?: string | undefined;
}

export const GFormReviewItem: React.FC<ItemProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);

  const Root = props.component ?? GFormReviewItemRoot;

  const inRowGroup = React.useContext(RowGroupContext);
  if (!inRowGroup) {
    return (
      <Root className={classes.root} ownerState={props}>
        <Grid2 data-type='item-grid'>
          <Grid2 data-type='item-label' size={{ xs: 3 }}><Typography className={classes.label}>{props.label}</Typography></Grid2>
          <Grid2 data-type='item-value' size={{ xs: 9 }}><Typography className={classes.body}>{props.children}</Typography></Grid2>
        </Grid2>
      </Root>
    );
  } else {
    return (
      <GFormReviewItemRoot className={classes.root}>
        {props.children}
      </GFormReviewItemRoot>
    );
  }
}


const useUtilityClasses = (ownerState: ItemProps) => {
  const slots = {
    root: ['root', ownerState.label],
    label: ['label', ownerState.label],
    body: ['body']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


const GFormReviewItemRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {
    '.GFormReviewItem-label': {
      fontWeight: 'bold'
    },
    '.GFormReviewItem-body': {

    }
  };
});
