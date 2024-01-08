import React from 'react';
import { Tabs, Tab, Box, TabProps, TabsProps, TextField, TextFieldProps, alpha } from '@mui/material';
import { styled } from "@mui/material/styles";
import { useIntl } from 'react-intl';


import { FlowExplorer, ServiceExplorer, DecisionExplorer } from './explorer';
import { Composer } from './context';
import { blueberry_whip, green_teal, sambucus } from 'components-colors';


const TextFieldRoot = styled(TextField)<TextFieldProps>(({ theme }) => ({

  color: blueberry_whip,
  backgroundColor: sambucus,
  '& .MuiOutlinedInput-input': {
    color: blueberry_whip,
  },
  '& .MuiOutlinedInput-root': {
    fontSize: '10pt',
    height: '2rem',
    '&.Mui-focused fieldset': {
      borderColor: sambucus,
    },
  },
  '& .MuiFormLabel-root': {
    color: blueberry_whip,
  },
  '& .MuiFormHelperText-root': {
    color: blueberry_whip,
    marginLeft: 1
  }
}));

const StyledTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    color: blueberry_whip,
    fontSize: '9pt',
    paddingLeft: '.5rem',
    paddingRight: '.5rem'
  },
  "&.Mui-selected": {
    color: green_teal,
    backgroundColor: alpha(green_teal, .2),
  },
}));

const StyledTabs = styled(Tabs)<TabsProps>(() => ({
  "& .MuiTabs-indicator": {
    backgroundColor: "unset",
  }
}));


const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const getLabel = (id: string) => intl.formatMessage({ id });

  const [tab, setTab] = React.useState("tabs.flows")
  const [searchString, setSearchString] = React.useState("");

  let component = <></>;
  if (tab === 'tabs.flows') {
    component = (<FlowExplorer />)
  } else if (tab === 'tabs.services') {
    component = (<ServiceExplorer />)
  } else if (tab === 'tabs.decisions') {
    component = (<DecisionExplorer />);
  }

  return (<Box sx={{ backgroundColor: sambucus, height: '100%' }}>
    <Box display="flex" >
      <StyledTabs value={tab} onChange={(_event: any, value: string) => setTab(value)}>
        <StyledTab label={getLabel("explorer.tabs.flows")} value='tabs.flows' />
        <StyledTab label={getLabel("explorer.tabs.services")} value='tabs.services' />
        <StyledTab label={getLabel("explorer.tabs.decisions")} value='tabs.decisions' />
      </StyledTabs>
      
      <Box alignSelf="center" sx={{ m: 1 }}>
        <TextFieldRoot focused placeholder={getLabel("explorer.tabs.search")}
          value={searchString}
          onChange={({ target }) => setSearchString(target.value)} />
      </Box>
    </Box>
    {component}
  </Box>)
}
export { Secondary }


