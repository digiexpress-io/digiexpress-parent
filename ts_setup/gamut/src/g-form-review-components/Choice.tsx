import React, { useContext } from 'react';
import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GFormReviewItem } from './Item';

export const GFormReviewChoice: React.FC<ItemProps> = ({ item, answerId }) => {
  const dC = useContext(GFormReviewContext);;
  const answer = dC.getAnswer(item.id, answerId);
  if (answer === null) { return null; }
  const valueSetEntry = dC.findValueSet(item.valueSetId)?.entries.find(entry => entry.id === answer);
  return (
    <GFormReviewItem label={dC.getTranslated(item.label)}>
      {dC.getTranslated(valueSetEntry?.label)}
    </GFormReviewItem>
  );
}
