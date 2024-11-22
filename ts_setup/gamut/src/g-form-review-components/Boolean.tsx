import React, { useContext } from 'react';
import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { useIntl } from 'react-intl';
import { GFormReviewItem } from './Item';

export const GFormReviewBoolean: React.FC<ItemProps> = ({ item, answerId }) => {
  const dC = useContext(GFormReviewContext);
  const answer = dC.getAnswer(item.id, answerId);
  const intl = useIntl();
  if (answer === null) { return null; }

  return (
    <GFormReviewItem label={dC.getTranslated(item.label)}>
      <span>{intl.formatMessage({ id: (answer === 'true' || answer === true) ? 'booleanValue.true' : 'booleanValue.false' })}</span>
    </GFormReviewItem>
  );
}
