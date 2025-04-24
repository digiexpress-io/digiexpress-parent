import React from 'react';

import { Box, IconButton, Typography, useTheme } from '@mui/material';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';

import { StencilComposerApi as Composer } from '../../stencil-setup';
import { _eveli_shell_useUtilityClasses as useUtilityClasses } from '@/eveli-shell';
import { useTenantConfig } from '@/api-tenant-config';
import { FormattedMessage } from 'react-intl';


const LocaleFilter: React.FC<{}> = () => {
  const theme = useTheme();
  const classes = useUtilityClasses();
  const {features} = useTenantConfig();
  const {site, session, actions} = Composer.useComposer();
  const locales = Object.values(site.locales);
  const selected = session.filter.locale ? session.filter.locale : '';

  if(!features.includes('stencil_locale_filter')){
    return (<></>);
  } 

  return (<>
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
  </>);
}

export { LocaleFilter }