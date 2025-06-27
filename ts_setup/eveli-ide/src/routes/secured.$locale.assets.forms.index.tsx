import React from 'react'
import { Box, useTheme } from '@mui/system';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';

import { useIntl, IntlProvider } from "react-intl";
import { useSnackbar } from "notistack";

import { DialobAdminView, messages, DialobAdminProps, DialobDashboardFetchProvider, DialobDashboardStateProvider, DialobAdminConfig } from '@/dialob-dashboard';
import { useFetch } from '@dxs-ts/eveli-fetch';

import { EveliSetup } from '@/eveli-setup';
import { EveliApp } from '@/eveli-app';
import { Button } from '@mui/material';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (<EveliApp main={Main} secondary={Secondary} toolbar={EveliSetup.Toolbar} />)

}


export const DialobAdminContainer: React.FC<DialobAdminProps> = ({ config, showNotification }) => {
  return (
    <DialobDashboardFetchProvider>
      <IntlProvider locale={config.language || 'en'} messages={messages[config.language]}>
        <DialobDashboardStateProvider config={config} showNotification={showNotification}>
          <DialobAdminView />
        </DialobDashboardStateProvider>
      </IntlProvider>
    </DialobDashboardFetchProvider>
  );
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
    <DialobAdminContainer showNotification={enqueueSnackbar} config={dialobAdminConfig} />
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