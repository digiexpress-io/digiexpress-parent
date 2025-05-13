import React from 'react';

import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FeedbackApi } from '@/api-feedback';


  /*
  Mistä aiheesta haluat antaa palautetta? = main
  Valitse aihe = sub
  Otsikko = title
  Kuvaus = question
  */


export const FeedbackContent: React.FC<{ feedback: FeedbackApi.FeedbackContent | undefined }> = ({ feedback }) => {
  const intl = useIntl();

  if (!feedback) {
    return;
  }
  return (<>
    <div style={{ marginTop: 25 }} />

    {feedback.main && <div style={{ marginBottom: 10 }}>
      <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.mainCategory' })}</Typography>
      <Typography>{feedback.main}</Typography>
    </div>
    }

    {feedback.sub &&
      <div style={{ marginBottom: 10 }}>
        <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.subCategory' })}</Typography>
        <Typography>{feedback.sub}</Typography>
      </div>
    }

    {feedback.title &&
      <div style={{ marginBottom: 10 }}>
        <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.customerTitle' })}</Typography>
        <Typography>{feedback.title}</Typography>
      </div>
    }
  </>
  )
}