import React from 'react';
import { useIntl } from "react-intl";
import { useSnackbar } from "notistack";

import { DialobAdmin, DialobAdminConfig } from "@dialob/dashboard-material";
import { useFetch } from '@dxs-ts/eveli-fetch';


export const DialobAdminView: React.FC = () => {

  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const { dialobUrl } = useFetch('dialob.GET', {});

  const dialobAdminConfig: DialobAdminConfig | undefined = React.useMemo(() => {
    return {
      csrf: undefined,
      dialobApiUrl: dialobUrl,
      setLoginRequired: () => { },
      setTechnicalError: () => { },
      language: intl.locale
    }
  }, [dialobUrl, intl.locale])

  return (
    <>
      {dialobAdminConfig && (
        <DialobAdmin showNotification={enqueueSnackbar} config={dialobAdminConfig} />
      )}
    </>

  )
}