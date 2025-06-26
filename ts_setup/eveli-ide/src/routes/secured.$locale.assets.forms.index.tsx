import React from 'react'
import { Box, useTheme } from '@mui/system';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';

import { useIntl, IntlProvider } from "react-intl";
import { useSnackbar } from "notistack";

import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFnsV3';

import {
  sv as svLocale,
  fi as fiLocale,
  et as etLocale,
  enGB as enLocale,
  ms as msLocale
} from 'date-fns/locale';
import { DialobAdminView, messages, DialobAdminProps, DialobDashboardFetchProvider, DialobDashboardStateProvider, DialobAdminConfig } from '@dialob/dashboard-material';
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

const localeMap: { [key: string]: any } = {
  en: enLocale,
  et: etLocale,
  fi: fiLocale,
  sv: svLocale,
  ms: msLocale,
};

export const DialobAdminContainer: React.FC<DialobAdminProps> = ({ config, showNotification }) => {
  return (
    <DialobDashboardFetchProvider>
      <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={localeMap[config.language]}>
        <IntlProvider locale={config.language || 'en'} messages={messages[config.language]}>
          <DialobDashboardStateProvider config={config} showNotification={showNotification}>
            <DialobAdminView />
          </DialobDashboardStateProvider>
        </IntlProvider>
      </LocalizationProvider>
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