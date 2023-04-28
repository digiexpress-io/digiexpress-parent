import React from "react";
import { Box, CircularProgress, Tabs, Tab, Typography, AppBar, Toolbar, useTheme } from "@mui/material";
import SwipeableViews from 'react-swipeable-views';

import {} from '@mui/icons-material';
import { FormattedMessage, useIntl } from 'react-intl';

import DeClient from '@declient';
import Styles from '@styles';

import DescriptorTable from '../DescriptorTable';
import DialobTable from '../DialobTable';
import StencilTable from '../StencilTable';
import HdesTable from '../HdesTable'


function a11yProps(index: number) {
  return {
    id: `full-width-tab-${index}`,
    'aria-controls': `full-width-tabpanel-${index}`,
  };
}



const Descriptors: React.FC<{}> = ({ }) => {
  const intl = useIntl();
  const nav = DeClient.useNav();
  const theme = useTheme();
  const service = DeClient.useService();
  const session = DeClient.useSession();
  const [state, setState] = React.useState<DeClient.DefinitionState>();
  const def = Object.values(session.head.definitions).find(() => true)!;

  const stencil = def.refs.find(ref => ref.type === 'STENCIL')?.tagName;
  const hdes = def.refs.find(ref => ref.type === 'HDES')?.tagName;
  const version = { version: def.version.substring(0, 6), stencil, hdes }


  const [value, setValue] = React.useState(0);

  const handleChange = (event: React.SyntheticEvent, newValue: number) => {
    setValue(newValue);
  };

  const handleChangeIndex = (index: number) => {
    setValue(index);
  };



  React.useEffect(() => {
    if (def) {
      service.definition(def.id).then(setState);
    }
  }, [def]);

  if (!state || !def) {
    return (<Box sx={{ display: 'flex' }}><CircularProgress /></Box>);
  }

  console.log();

  return (<Box sx={{ backgroundColor: 'mainContent.main' }}>
    <AppBar position="sticky" sx={{ backgroundColor: 'mainContent.main', boxShadow: 'unset', color: 'unset' }}>
      
      <Box sx={{mt: 2, ml: 3}}>
        <Typography><FormattedMessage id="descriptors.tabs.desc_1" /></Typography>
      </Box>

      <Toolbar>
        <Tabs indicatorColor="secondary" textColor="inherit" variant="fullWidth"
          value={value}
          onChange={handleChange}>

          <Tab label={<FormattedMessage id="descriptors.tabs.overview" />} {...a11yProps(0)} />
          <Tab label={<FormattedMessage id="descriptors.tabs.dialob" />} {...a11yProps(1)} />
          <Tab label={<FormattedMessage id="descriptors.tabs.stencil" />} {...a11yProps(1)} />
          <Tab label={<FormattedMessage id="descriptors.tabs.hdes" />} {...a11yProps(2)} />
          <Tab label={<FormattedMessage id="descriptors.tabs.errors" />} {...a11yProps(2)} />
        </Tabs>
      </Toolbar>
    </AppBar>

    <SwipeableViews axis='x' index={value} onChangeIndex={handleChangeIndex}>
      <Styles.TabPanel value={value} index={0} dir={theme.direction}><DescriptorTable def={state} /></Styles.TabPanel>
      <Styles.TabPanel value={value} index={1} dir={theme.direction}><DialobTable /></Styles.TabPanel>
      <Styles.TabPanel value={value} index={2} dir={theme.direction}><StencilTable /></Styles.TabPanel>
      <Styles.TabPanel value={value} index={3} dir={theme.direction}><HdesTable /></Styles.TabPanel>

    </SwipeableViews>

  </Box>);

}

export default Descriptors;
