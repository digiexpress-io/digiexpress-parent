import React from 'react';
import { useIntl } from "react-intl";
import { useSnackbar } from "notistack";

import { DialobAdmin, DialobAdminConfig } from "@dialob/dashboard-material";
import { useConfig } from "../../context/ConfigContext";

export const DialobAdminView: React.FC = () => {
  const { serviceUrl } = useConfig();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const dialobAdminConfig: DialobAdminConfig | undefined = React.useMemo(() => {

    if (serviceUrl) {
      return {
        csrf: undefined,
        dialobApiUrl: serviceUrl + 'rest/api/assets/dialob/proxy',
        setLoginRequired: () => { },
        setTechnicalError: () => { },
        language: intl.locale
      }
    } else {
      return undefined;
    }
  }, [serviceUrl, intl.locale])

  return (
    <>
      {dialobAdminConfig && (
        <DialobAdmin showNotification={enqueueSnackbar} config={dialobAdminConfig} />
      )}
    </>

  )
}