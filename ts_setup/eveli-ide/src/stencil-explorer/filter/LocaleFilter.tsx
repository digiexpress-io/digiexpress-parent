import React from 'react';

import { IconButton, Typography } from '@mui/material';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';

import { StencilComposerApi as Composer } from '../../stencil-setup';
import { _eveli_shell_useUtilityClasses as useUtilityClasses } from '@/eveli-shell';
import { EveliTenantFeatureEnabled } from '@/api-tenant-config';
import { FormattedMessage } from 'react-intl';


const LocaleFilter: React.FC<{}> = () => {
  const classes = useUtilityClasses();
  const {site, session, actions} = Composer.useComposer();
  const locales = Object.values(site.locales);
  const selected = session.filter.locale ? session.filter.locale : '';


  return (<EveliTenantFeatureEnabled id='STENCIL_LOCALE_FILTER'>
    {locales.map((locale) => (
      <div key={locale.id}>
        <IconButton
          {...(locale.id === selected ? { disabled: true, className: classes.itemActive } : {})}
          onClick={() => actions.handleLocaleFilter(locale.id)}
        >
          <SettingsOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id={`locale.${locale.body.value}`} /></Typography>
      </div>
    ))}
  </EveliTenantFeatureEnabled>);
}

export { LocaleFilter }