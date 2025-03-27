import React from 'react'
import { Box, useTheme } from '@mui/system';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';

import { useIntl } from "react-intl";
import { useSnackbar } from "notistack";

import { DialobAdmin, DialobAdminConfig } from "@dialob/dashboard-material";
import { useFetch } from '@dxs-ts/eveli-fetch';
import { createFileRoute } from '@tanstack/react-router'


import { EveliSetup } from '@/eveli-setup';
import { useLocale } from '@/api-locale';
import { EveliApp } from '@/eveli-app';
import { Button } from '@mui/material';

export const Route = createFileRoute('/secured/$locale/assets/forms/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();

  const { setLocale } = useLocale();
  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<EveliApp main={Main} secondary={Secondary} toolbar={EveliSetup.Toolbar} />)

}

const Main: React.FC<{}> = () => {
  const intl = useIntl();
  const theme = useTheme();

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

  return (<Box sx={{ p: theme.spacing(1) }}>
    <DialobAdmin showNotification={enqueueSnackbar} config={dialobAdminConfig} />
  </Box>)
}

const Secondary: React.FC = () => {
  const intl = useIntl();
  const theme = useTheme();

  return (<Box p={theme.spacing(1)}>
    <Button variant='explorerInactive'
      startIcon={<HelpOutlineOutlinedIcon />}
      onClick={() => window.open("https://github.com/dialob/dialob-parent/wiki", "_blank")}>
      {intl.formatMessage({ id: 'menu.help' })}
    </Button>
  </Box>)
}