import React from 'react';
import { ListItem, ListItemIcon, ListItemText, List, generateUtilityClass, styled, useThemeProps } from '@mui/material';
import { CheckOutlined, RemoveOutlined } from '@mui/icons-material';
import composeClasses from '@mui/utils/composeClasses';

import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GFormReviewItem } from './GFormReviewItem';





const MUI_NAME = 'GFormReviewMultiChoice';

export interface GFormReviewMultiChoiceClasses {
  root: string;
}

export type GFormReviewMultiChoiceClassKey = keyof GFormReviewMultiChoiceClasses;


export const GFormReviewMultiChoice: React.FC<ItemProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);

  const answer: null | string[] = dC.getAnswer(props.item.id, props.answerId);
  if (answer === null || answer.length === 0) { return null; }
  const valueSet = dC.findValueSet(props.item.valueSetId)

  const renderEntry = (entry: { id: string, label?: Record<string, string> }, answered: boolean) => {
    return (
      <ListItem key={entry.id}>
        {answered ?
          <ListItemIcon><CheckOutlined /></ListItemIcon>
          : <ListItemIcon><RemoveOutlined /></ListItemIcon>}
        <ListItemText>{dC.getTranslated(entry.label)}</ListItemText>
      </ListItem>)
  }

  const answeredEntries = valueSet?.entries.filter(entry => answer.includes(entry.id)).map(entry => renderEntry(entry, true));
  const notAnsweredEntries = valueSet?.entries.filter(entry => !answer.includes(entry.id)).map(entry => renderEntry(entry, false));

  const Root = props.component ?? GFormReviewMultiChoiceRoot;
  return (
    <Root className={classes.root} ownerState={props}>
      <GFormReviewItem label={dC.getTranslated(props.item.label)}>
        <List dense>
          {answeredEntries}
          {notAnsweredEntries}
        </List>
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


const GFormReviewMultiChoiceRoot = styled("div", {
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

