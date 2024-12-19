import React from 'react';
import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled, useThemeProps } from '@mui/material';
import { useIntl } from 'react-intl';

import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GFormReviewItem } from './GFormReviewItem';


const MUI_NAME = 'GFormReviewDecimal';

export interface GFormReviewDecimalClasses {
  root: string;
}

export type GFormReviewDecimalClassKey = keyof GFormReviewDecimalClasses;



export const GFormReviewDecimal: React.FC<ItemProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);

  const intl = useIntl();
  const answer = dC.getAnswer(props.item.id, props.answerId);
  if (answer === null) {
    return null
  }

  const Root = props.component ?? GFormReviewDecimalRoot;
  return (
    <Root className={classes.root} ownerState={props}>
      <GFormReviewItem label={dC.getTranslated(props.item.label)}>
        <React.Fragment>
          {intl.formatNumber(answer)}
        </React.Fragment>
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


const GFormReviewDecimalRoot = styled("div", {
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
