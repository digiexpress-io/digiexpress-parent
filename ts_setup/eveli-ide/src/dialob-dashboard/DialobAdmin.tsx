import { DialobAdminView } from "./DialobAdminView";
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { IntlProvider } from "react-intl";
import { messages } from './intl';

import {
  sv as svLocale,
  fi as fiLocale,
  et as etLocale,
  enGB as enLocale,
  ms as msLocale
} from 'date-fns/locale';
import { DialobAdminProps } from "./types";
import { DialobDashboardFetchProvider } from "./context";
import { DialobDashboardStateProvider } from "./context/DialobDashboardStateContext";

const localeMap: { [key: string]: any } = {
  en: enLocale,
  et: etLocale,
  fi: fiLocale,
  sv: svLocale,
  ms: msLocale,
};

export const DialobAdmin: React.FC<DialobAdminProps> = ({ config, showNotification, onOpenForm }) => {
  return (
    <DialobDashboardFetchProvider>
      <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={localeMap[config.language]}>
        <IntlProvider locale={config.language || 'en'} messages={messages[config.language]}>
          <DialobDashboardStateProvider config={config} showNotification={showNotification} onOpenForm={onOpenForm}>
            <DialobAdminView />
          </DialobDashboardStateProvider>
        </IntlProvider>
      </LocalizationProvider>
    </DialobDashboardFetchProvider>
  );
}
