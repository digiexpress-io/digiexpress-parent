import React from 'react';
import { generateUtilityClass, styled, useThemeProps } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GFormReviewItem } from './GFormReviewItem';
import { TimestampFormatter } from './TimestampFormatter';



const MUI_NAME = 'GFormReviewTime';

export interface GFormReviewTimeClasses {
  root: string;
}

export type GFormReviewTimeClassKey = keyof GFormReviewTimeClasses;


export const GFormReviewTime: React.FC<ItemProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);;


  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);

  const answer = dC.getAnswer(props.item.id, props.answerId);

  if (answer === null) {
    return null
  }

  const Root = props.component ?? GFormReviewTimeRoot;
  return (
    <Root className={classes.root} ownerState={props}>
      <GFormReviewItem label={dC.getTranslated(props.item.label)}>
        <TimestampFormatter timestamp={answer} format='time' />
      </GFormReviewItem>
    </Root>
  );
}


const useUtilityClasses = (ownerState: ItemProps) => {
  const slots = {
    root: ['root', ownerState.item.id],

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


const GFormReviewTimeRoot = styled("div", {
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
