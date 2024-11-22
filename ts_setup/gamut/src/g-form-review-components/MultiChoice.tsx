import React, { useContext } from 'react';
import { ListItem, ListItemIcon, ListItemText, List } from '@mui/material';
import { CheckOutlined, RemoveOutlined } from '@mui/icons-material';
import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GFormReviewItem } from './Item';

export const GFormReviewMultiChoice: React.FC<ItemProps> = ({ item, answerId }) => {
  const dC = useContext(GFormReviewContext);;
  const answer: null | string[] = dC.getAnswer(item.id, answerId);
  if (answer === null || answer.length === 0) { return null; }
  const valueSet = dC.findValueSet(item.valueSetId)

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

  return (
    <GFormReviewItem label={dC.getTranslated(item.label)}>
      <List dense>
        {answeredEntries}
        {notAnsweredEntries}
      </List>
    </GFormReviewItem>
  );
}
