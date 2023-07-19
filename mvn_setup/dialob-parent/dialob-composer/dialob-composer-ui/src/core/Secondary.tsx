import React from 'react';
import { Tabs, Tab, Box, TabProps, TabsProps, TextField, TextFieldProps, alpha } from '@mui/material';
import { styled } from "@mui/material/styles";
import { useIntl } from 'react-intl';
import { FormrevExplorer } from './explorer';


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
  const getLabel = (id: string) => intl.formatMessage({ id });

  const [tab, setTab] = React.useState("tabs.revs")
  const [searchString, setSearchString] = React.useState("");

  let component = <></>;
  if (tab === 'tabs.revs') {
    component = (<FormrevExplorer />)
  }

  return (<Box sx={{ backgroundColor: "explorer.main", height: '100%' }}>
    <Box display="flex" >
      <StyledTabs value={tab} onChange={(_event: any, value: string) => setTab(value)}>
        <StyledTab label={getLabel("explorer.tabs.revs")} value='tabs.revs' />
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


