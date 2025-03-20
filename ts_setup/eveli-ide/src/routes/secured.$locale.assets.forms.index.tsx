import React from 'react'
import { Box } from '@mui/system';

import { useIntl } from "react-intl";
import { useSnackbar } from "notistack";

import { DialobAdmin, DialobAdminConfig } from "@dialob/dashboard-material";
import { useFetch } from '@dxs-ts/eveli-fetch';
import { createFileRoute } from '@tanstack/react-router'

import { useLocale, EveliApp } from '@/burger'

import { Secondary } from '../frontdesk/Secondary';
import { Toolbar } from '../frontdesk/Toolbar';

export const Route = createFileRoute('/secured/$locale/assets/forms/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();

  const { setLocale } = useLocale();
  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} />)

}

const Main: React.FC<{}> = () => {
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

  return (<Box sx={{ p: 1 }}>
    <DialobAdmin showNotification={enqueueSnackbar} config={dialobAdminConfig} />
  </Box>)
}