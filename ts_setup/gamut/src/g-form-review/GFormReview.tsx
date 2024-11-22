import React from 'react';
import { useThemeProps } from '@mui/system';
import { CircularProgress } from '@mui/material';
import { useDialobReview } from '../api-dialob';

import { GFormReviewRoot, MUI_NAME } from './useUtilityClasses';
import { DEFAULT_ITEM_CONFIG, GFormReviewQuestionnaire, GFormReviewContext, GFormReviewContextType, ItemconfigType } from '../g-form-review-components';


export interface GFormReviewProps {
  formId: string
  itemConfig?: ItemconfigType //overwrite defaults
}
export const GFormReview: React.FC<GFormReviewProps> = (initProps) => {
  const { isPending, review } = useDialobReview({ id: initProps.formId })

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  if (isPending) {
    return <CircularProgress />;
  }

  if (!review) {
    return <>Unavailable</>;
  }

  const { session, form } = review;
  const language: string = session.metadata.language;
  const documentTitle = session.metadata.label;

  const rootItem = (() => {
    for (let id in form.data) {
      if (form.data[id].type === 'questionnaire') return form.data[id];
    }
    return null;
  })();

  const contextValue = new GFormReviewContextType(session, form, language, props.itemConfig ?? DEFAULT_ITEM_CONFIG);
  return (
    <GFormReviewContext.Provider value={contextValue}>
      <GFormReviewRoot>
        <GFormReviewQuestionnaire item={rootItem} title={documentTitle} />
      </GFormReviewRoot>
    </GFormReviewContext.Provider>)
}

