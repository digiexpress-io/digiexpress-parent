import React from 'react';
import { generateUtilityClass, styled, useThemeProps } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GFormReviewItem } from './GFormReviewItem';



const MUI_NAME = 'GFormReviewText';

export interface GFormReviewTextClasses {
  root: string;
}

export type GFormReviewTextClassKey = keyof GFormReviewTextClasses;


export const GFormReviewText: React.FC<ItemProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);


  const answer = dC.getAnswer(props.item.id, props.answerId);
  if (answer === null) {
    return null
  }

  const Root = props.component ?? GFormReviewTextRoot;
  return (
    <Root className={classes.root} ownerState={props}>
      <GFormReviewItem label={dC.getTranslated(props.item.label)}>
        {answer}
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


const GFormReviewTextRoot = styled("div", {
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