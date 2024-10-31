import React from 'react';
import { useIntl } from "react-intl";
import { useSnackbar } from "notistack";

import { DialobAdmin, DialobAdminConfig } from "@dialob/dashboard-material";
import { useConfig } from "../../context/ConfigContext";

export const DialobAdminView: React.FC = () => {
  const config = useConfig();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const dialobAdminConfig: DialobAdminConfig | undefined = React.useMemo(() => {
    if (config.dialobComposerUrl) {
      return {
        csrf: undefined,
        dialobApiUrl: config.dialobComposerUrl.split("/composer")[0],
        setLoginRequired: () => { },
        setTechnicalError: () => { },
        language: intl.locale
      }
    } else {
      return undefined;
    }
  }, [config.dialobComposerUrl, intl.locale])

  return (
    <>
      {dialobAdminConfig && (
        <DialobAdmin showNotification={enqueueSnackbar} config={dialobAdminConfig} />
      )}
    </>

  )
}