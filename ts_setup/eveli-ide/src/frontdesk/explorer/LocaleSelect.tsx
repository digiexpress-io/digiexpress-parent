import React from 'react';
import { Tabs, Tab, TabProps, TabsProps, styled } from '@mui/material';

import { frontdeskIntl } from '../intl';
import { useLocale } from '../context';


const StyledTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    color: theme.palette.explorerItem.main,
  },
  "&.Mui-selected": {
    color: theme.palette.explorerItem.dark,
  }
}));

const StyledTabs = styled(Tabs)<TabsProps>(({ theme }) => ({
  "& .MuiTabs-indicator": {
    backgroundColor: theme.palette.explorerItem.dark,
    marginRight: "49px"
  }
}));


const locales = Object.keys(frontdeskIntl).map((key) => ({
  id: key,
  body: {
    value: key.toUpperCase(),
    enabled: key === 'fi'
  },
}));

const LocaleSelect: React.FC<{}> = () => {
  const { locale, setLocale } = useLocale();

  return (<StyledTabs orientation="vertical" sx={{ borderRight: 1, borderColor: 'explorerItem.dark', maxHeight: '200px' }} value={locale}
    onChange={(_event, newValue) => setLocale(newValue)}
    variant="scrollable"
    scrollButtons="auto">{
      locales.map((locale) => <StyledTab key={locale.id} value={locale.id} label={locale.body.value} />)
    }
  </StyledTabs>);
}

export { LocaleSelect }