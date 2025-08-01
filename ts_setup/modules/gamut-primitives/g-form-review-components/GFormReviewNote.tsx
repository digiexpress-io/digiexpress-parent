import React from 'react';
import { generateUtilityClass, styled, useThemeProps } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GMarkdown } from '../g-md';



const MUI_NAME = 'GFormReviewNote';

export interface GFormReviewNoteClasses {
  root: string;
}

export type GFormReviewNoteClassKey = keyof GFormReviewNoteClasses;


export const GFormReviewNote: React.FC<ItemProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);

  const label = dC.getTranslated(props.item.label);

  const Root = props.component ?? GFormReviewNoteRoot;
  return (
    <Root className={classes.root} ownerState={props}>
      <GMarkdown children={dC.substituteVariables(label)} />
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


const GFormReviewNoteRoot = styled("div", {
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

