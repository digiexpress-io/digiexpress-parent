import React, { useContext } from 'react';
import { useIntl } from 'react-intl';
import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { GFormReviewItem } from './Item';

export const GFormReviewDecimal: React.FC<ItemProps> = ({ item, answerId }) => {
  const dC = useContext(GFormReviewContext);;
  const intl = useIntl();
  const answer = dC.getAnswer(item.id, answerId);
  if (answer === null) { return null; }
  return (
    <GFormReviewItem label={dC.getTranslated(item.label)}>
      <React.Fragment>
        {intl.formatNumber(answer)}
      </React.Fragment>
    </GFormReviewItem>
  );
}
