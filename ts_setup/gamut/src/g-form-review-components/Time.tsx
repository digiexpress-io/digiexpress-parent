import React, { useContext } from 'react';
import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GFormReviewItem } from './Item';
import { TimestampFormatter } from './TimestampFormatter';

export const GFormReviewTime: React.FC<ItemProps> = ({ item, answerId }) => {
  const dC = useContext(GFormReviewContext);;
  const answer = dC.getAnswer(item.id, answerId);

  if (answer === null) { return null; }
  return (
    <GFormReviewItem label={dC.getTranslated(item.label)}>
      <TimestampFormatter timestamp={answer} format='time' />
    </GFormReviewItem>
  );
}
