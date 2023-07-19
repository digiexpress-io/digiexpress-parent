import React from 'react';

import { Tabs, Tab, TabProps, TabsProps } from '@mui/material';
import { styled } from "@mui/material/styles";

import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';

import { Composer } from '../../context';

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


const LocaleFilter: React.FC<{}> = () => {
  const {site, session, actions} = Composer.useComposer();
  const locales = Object.values(site.locales);
  const selected = session.filter.locale ? session.filter.locale : '';

  return (<StyledTabs orientation="vertical" sx={{ borderRight: 1, borderColor: 'explorerItem.dark', maxHeight: '200px' }} value={selected}
    onChange={(_event, newValue) => actions.handleLocaleFilter(newValue)}
    variant="scrollable"
    scrollButtons="auto">{
      locales.map((locale) => <StyledTab key={locale.id} value={locale.id} label={locale.body.value} />)
    }
    <StyledTab icon={<SettingsOutlinedIcon />} value=''/>
  </StyledTabs>);
}

export { LocaleFilter }