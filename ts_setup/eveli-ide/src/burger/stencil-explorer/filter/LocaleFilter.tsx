import React from 'react';

import { Tabs, Tab } from '@mui/material';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';

import { StencilComposerApi as Composer } from '../../stencil-setup';


const LocaleFilter: React.FC<{}> = () => {
  const {site, session, actions} = Composer.useComposer();
  const locales = Object.values(site.locales);
  const selected = session.filter.locale ? session.filter.locale : '';

  return (<Tabs orientation="vertical" sx={{ maxHeight: '200px' }} value={selected}
    onChange={(_event, newValue) => actions.handleLocaleFilter(newValue)}
    variant="scrollable"
    scrollButtons="auto">{
      locales.map((locale) => <Tab key={locale.id} value={locale.id} label={locale.body.value} />)
    }
    <Tab icon={<SettingsOutlinedIcon />} value=''/>
  </Tabs>);
}

export { LocaleFilter }