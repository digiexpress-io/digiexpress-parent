import { createFileRoute } from '@tanstack/react-router'
import React from 'react'
import { Box, useTheme } from '@mui/system';
import { Button } from '@mui/material';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';

import { useIntl, IntlProvider } from "react-intl";
import { useSnackbar } from "notistack";

import { useFetch } from '@dxs-ts/envir-fetch';
import { useTenantConfigFeatures } from '@dxs-ts/eveli-api';
import { DialobDashboardSmart, DialobAdminView, dialob_messages, DialobDashboardFetchProvider, DialobDashboardStateProvider, DialobAdminConfig  } from '@dxs-ts/eveli-primitives';

import { EveliSetup } from '../eveli-setup';
import { EveliApp } from '../eveli-app';


export const Route = createFileRoute('/secured/$locale/assets/forms/')({
  component: Component,
})

function Component() {
  const { isEnabled } = useTenantConfigFeatures();
  const main = isEnabled('DIALOB_DASHBOARD_SMART') ? MainSmart : Main;

  return (<EveliApp main={main} secondary={Secondary} toolbar={EveliSetup.Toolbar} />)

}


const MainSmart: React.FC<{}> = () => {
  const intl = useIntl();
  const theme = useTheme();


  return (<Box sx={{ p: theme.spacing(1) }}>
    <DialobDashboardSmart />
  </Box>)
}


const Main: React.FC<{}> = () => {
  const intl = useIntl();
  const theme = useTheme();

  const { enqueueSnackbar } = useSnackbar();
  const { dialobUrl } = useFetch('dialob.GET', {});
  const config: DialobAdminConfig | undefined = React.useMemo(() => {
    return {
      csrf: undefined,
      dialobApiUrl: dialobUrl,
      setLoginRequired: () => { },
      setTechnicalError: () => { },
      language: intl.locale
    }
  }, [dialobUrl, intl.locale])

  return (<Box sx={{ p: theme.spacing(1) }}>
    

    <DialobDashboardFetchProvider>
      <IntlProvider locale={config.language || 'en'} messages={dialob_messages[config.language]}>
        <DialobDashboardStateProvider config={config} showNotification={enqueueSnackbar}>
          <DialobAdminView />
        </DialobDashboardStateProvider>
      </IntlProvider>
    </DialobDashboardFetchProvider>

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