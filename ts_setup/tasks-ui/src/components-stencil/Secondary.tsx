import React from 'react';
import { Tabs, Tab, Box, TabProps, TabsProps, TextFieldProps, TextField, alpha } from '@mui/material';
import { styled } from "@mui/material/styles";
import { useIntl } from 'react-intl';
import Burger from 'components-burger';
import { ArticleExplorer, WorkflowExplorer, LinkExplorer, SearchExplorer } from './explorer';
import { blueberry_whip, green_teal, sambucus } from 'components-colors';


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


const StyledSearch = styled(TextField)<TextFieldProps>(({ theme }) => ({
  color: blueberry_whip,
  backgroundColor: sambucus,
  '& .MuiOutlinedInput-input': {
    color: blueberry_whip,
  },
  '& .MuiOutlinedInput-root': {
    fontSize: '10pt',
    height: '2rem',
    '&.Mui-focused fieldset': {
      borderColor: green_teal,
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



const SecondaryExplorer: React.FC<{}> = () => {
  const intl = useIntl();
  const getLabel = (id: string) => intl.formatMessage({ id });
  const [tab, setTab] = React.useState("toolbar.articles");
  const [searchString, setSearchString] = React.useState<string>("");

  let component = <></>;

  if (tab === 'toolbar.services') {
    component = (<WorkflowExplorer searchString={searchString.toLocaleLowerCase()} />)
  } else if (tab === 'toolbar.links') {
    component = (<LinkExplorer searchString={searchString.toLocaleLowerCase()} />)
  } else {
    component = <ArticleExplorer searchString={searchString.toLocaleLowerCase()} />;
  }

  return (<>
    <Box display="flex" >
      <StyledTabs value={tab} onChange={(_event, value) => setTab(value)}>
        <StyledTab label={getLabel("explorer.tabs.articles")} value='toolbar.articles' />
        <StyledTab label={getLabel("explorer.tabs.services")} value='toolbar.services' />
        <StyledTab label={getLabel("explorer.tabs.links")} value='toolbar.links' />
      </StyledTabs>
      <Box alignSelf="center" sx={{ m: 1 }}>
        <StyledSearch focused
          type="search"
          placeholder={getLabel("explorer.tabs.search")}
          value={searchString}
          onChange={({ target }) => setSearchString(target.value)} />
      </Box>
    </Box>
    {component}
  </>);
}


const Secondary: React.FC<{}> = () => {
  const {session} = Burger.useSecondary();

  let component = <></>;
  if (session.secondary === 'toolbar.search') {
    component = (<SearchExplorer />)
  } else {
    component = <SecondaryExplorer />;
  }
  return (<Box sx={{ backgroundColor: sambucus, height: '100%' }}>{component}</Box>)
}
export { Secondary }


