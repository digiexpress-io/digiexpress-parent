import React from 'react';
import { Tabs, Tab, Box, TabProps, TabsProps, TextField, TextFieldProps, alpha, Typography, Button } from '@mui/material';
import { styled } from "@mui/material/styles";
import { FormattedMessage, useIntl } from 'react-intl';


import { FlowExplorer, ServiceExplorer, DecisionExplorer } from './explorer';
import { Composer } from './context';


const TextFieldRoot = styled(TextField)<TextFieldProps>(({ theme }) => ({

  color: theme.palette.explorerItem.main,
  backgroundColor: theme.palette.explorer.main,
  '& .MuiOutlinedInput-input': {
    color: theme.palette.explorerItem.main,
  },
  '& .MuiOutlinedInput-root': {
    fontSize: '10pt',
    height: '2rem',
    '&.Mui-focused fieldset': {
      borderColor: theme.palette.explorerItem.dark,
    },
  },
  '& .MuiFormLabel-root': {
    color: theme.palette.explorerItem.main,
  },
  '& .MuiFormHelperText-root': {
    color: theme.palette.explorerItem.main,
    marginLeft: 1
  }
}));

const StyledTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    color: theme.palette.explorerItem.main,
    fontSize: '9pt',
    paddingLeft: '.5rem',
    paddingRight: '.5rem'
  },
  "&.Mui-selected": {
    color: theme.palette.explorerItem.dark,
    backgroundColor: alpha(theme.palette.explorerItem.dark, .2),
  },
}));

const StyledTabs = styled(Tabs)<TabsProps>(() => ({
  "& .MuiTabs-indicator": {
    backgroundColor: "unset",
  }
}));


const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const activeBranch = Composer.useBranchName();
  const branchName = activeBranch || intl.formatMessage({ id: 'explorer.active.branch.default' });
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

  return (<Box sx={{ backgroundColor: "explorer.main", height: '100%' }}>
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
    <Box sx={{ position: 'absolute', bottom: '2%', left: '15%' }}>
      <Typography sx={{ color: 'white' }}><FormattedMessage id='explorer.active.branch' values={{ name: branchName }} /></Typography>
    </Box>

  </Box>)
}
export { Secondary }


