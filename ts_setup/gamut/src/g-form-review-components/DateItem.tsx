import React, { useContext } from 'react';
import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { FormattedDate } from 'react-intl';
import { GFormReviewItem } from './Item';

export const GFormReviewDateItem: React.FC<ItemProps> = ({ item, answerId }) => {
  const dC = useContext(GFormReviewContext);;
  const answer = dC.getAnswer(item.id, answerId);
  if (answer === null) { return null; }
  // TODO:: Admir, Muamer error <Components.TimestampFormatter timestamp={answer} format='date' />
  return (
    <GFormReviewItem label={dC.getTranslated(item.label)}>
      <FormattedDate value={answer} />
    </GFormReviewItem>
  );

}