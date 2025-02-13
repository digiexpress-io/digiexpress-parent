import React from 'react';

import { Tabs, Tab, TabProps, TabsProps } from '@mui/material';
import { styled } from "@mui/material/styles";

import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';

import { Composer } from '../../context';
import { blueberry_whip, green_teal } from 'components-colors';

const StyledTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    color: blueberry_whip,
  },
  "&.Mui-selected": {
    color: green_teal,
  }
}));

const StyledTabs = styled(Tabs)<TabsProps>(({ theme }) => ({
  "& .MuiTabs-indicator": {
    backgroundColor: green_teal,
    marginRight: "49px"
  }
}));


const LocaleFilter: React.FC<{}> = () => {
  const {site, session, actions} = Composer.useComposer();
  const locales = Object.values(site.locales);
  const selected = session.filter.locale ? session.filter.locale : '';

  return (<StyledTabs orientation="vertical" sx={{ borderRight: 1, borderColor: green_teal, maxHeight: '200px' }} value={selected}
    onChange={(_event, newValue) => actions.handleLocaleFilter(newValue)}
    variant="scrollable"
    scrollButtons="auto">{
      locales.map((locale) => <StyledTab key={locale.id} value={locale.id} label={locale.body.value} />)
    }
    <StyledTab icon={<SettingsOutlinedIcon />} value=''/>
  </StyledTabs>);
}

export { LocaleFilter }