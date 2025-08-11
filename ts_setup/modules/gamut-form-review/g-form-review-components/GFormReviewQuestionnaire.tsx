import React from 'react';
import { generateUtilityClass, Grid2, styled, useThemeProps } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

import { GFormReviewContext } from './GFormReviewContext';
import type { ItemProps } from './componentTypes';



const MUI_NAME = 'GFormReviewQuestionnaire';

export interface GFormReviewQuestionnaireClasses {
  root: string;
}

export type GFormReviewQuestionnaireClassKey = keyof GFormReviewQuestionnaireClasses;


export interface QuestionnaireItemProps extends ItemProps {
  title: string;
  item: {
    items?: string[];
  };
}

export const GFormReviewQuestionnaire: React.FC<QuestionnaireItemProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);

  const items = props.item.items ? props.item.items.map(id => dC.createItem(id, null, true)) : null;
  const Root = props.component ?? GFormReviewQuestionnaireRoot;
  return (
    <Root className={classes.root} ownerState={props}>
      {items}
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


const GFormReviewQuestionnaireRoot = styled(Grid2, {
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
