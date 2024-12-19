import React from 'react';
import { styled, useThemeProps } from '@mui/system';
import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass } from '@mui/material';

import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GFormReviewItem } from './GFormReviewItem';


const MUI_NAME = 'GFormReviewChoice';

export interface GFormReviewChoiceClasses {
  root: string;
}

export type GFormReviewChoiceClassKey = keyof GFormReviewChoiceClasses;


export const GFormReviewChoice: React.FC<ItemProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);

  const answer = dC.getAnswer(props.item.id, props.answerId);
  if (answer === null) {
    return null;
  }

  const valueSetEntry = dC.findValueSet(props.item.valueSetId)?.entries.find(entry => entry.id === answer);

  const Root = props.component ?? GFormReviewChoiceRoot;
  return (
    <Root className={classes.root} ownerState={props}>
      <GFormReviewItem label={dC.getTranslated(props.item.label)}>
        {dC.getTranslated(valueSetEntry?.label)}
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


const GFormReviewChoiceRoot = styled("div", {
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