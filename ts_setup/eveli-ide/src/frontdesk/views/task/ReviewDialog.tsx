import React, { useContext, useEffect, useState } from 'react';
import { useFetch } from '../../hooks/useFetch';
import { DialobReviewDialog } from '../../dialob/DialobReviewDialog';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { CircularProgress } from '@mui/material';
import { useConfig } from '../../context/ConfigContext';
import { TaskLink } from '../../types/task/TaskLink';

export type ReviewDialogProps = {
  closeDialog: ()=>void
  link: TaskLink
}

export const ReviewDialog: React.FC<ReviewDialogProps> = ({closeDialog, link}) => {

  const config = useConfig();
  const {response:sessionResponse, error:sessionLoadError} = useFetch(`${config.dialobApiUrl}/questionnaires/${link.linkAddress}`);
  const [form, setForm] = useState<any>(null);
  const session = useContext(SessionRefreshContext);
  useEffect(()=> {
    if (sessionResponse) {
      let formName = (sessionResponse as any).metadata?.formId;
      session.cFetch(config.dialobApiUrl + '/forms/' + formName,{
        headers: {
          'Accept': 'application/json'
        },
      })
      .then(response=>response.json())
      .then(json=> {
        setForm(json);
      })
    }
  }, [sessionResponse, session, config.dialobApiUrl])

  if (form && (sessionResponse||sessionLoadError)) { 
    return (
      <DialobReviewDialog form={form} session={sessionResponse} closeDialog={closeDialog} error={sessionLoadError} />
    )
  }
  return (
    <CircularProgress />
  )
}