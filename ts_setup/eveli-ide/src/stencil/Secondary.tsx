import React from 'react';
import { Tabs, Tab, Box, TabProps, TabsProps, TextFieldProps, TextField, alpha } from '@mui/material';
import { styled } from "@mui/material/styles";
import { useIntl } from 'react-intl';
import * as Burger from '@/burger';
import { ArticleExplorer, WorkflowExplorer, LinkExplorer, SearchExplorer } from './explorer';


const StyledTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    color: theme.palette.primary.contrastText,
    fontSize: '9pt',
    paddingLeft: '.5rem',
    paddingRight: '.5rem'
  },
  "&.Mui-selected": {
    color: theme.palette.secondary.contrastText,
    backgroundColor: alpha(theme.palette.secondary.contrastText, .2),
  },
}));

const StyledTabs = styled(Tabs)<TabsProps>(() => ({
  "& .MuiTabs-indicator": {
    backgroundColor: "unset",
  }
}));


const StyledSearch = styled(TextField)<TextFieldProps>(({ theme }) => ({
  color: theme.palette.primary.contrastText,
  backgroundColor: theme.palette.secondary.main,
  '& .MuiOutlinedInput-input': {
    color: theme.palette.primary.contrastText,
  },
  '& .MuiOutlinedInput-root': {
    fontSize: '10pt',
    height: '2rem',
    '&.Mui-focused fieldset': {
      borderColor: theme.palette.secondary.contrastText,
    },
  },
  '& .MuiFormLabel-root': {
    color: theme.palette.primary.contrastText,
  },
  '& .MuiFormHelperText-root': {
    color: theme.palette.primary.contrastText,
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
    <Box display="flex" flexDirection='column'>

    <StyledSearch focused  sx={{ mx: 1 }}
          type="search"
          placeholder={getLabel("explorer.tabs.search")}
          value={searchString}
          onChange={({ target }) => setSearchString(target.value)} />

      <StyledTabs value={tab} onChange={(_event, value) => setTab(value)}>
        <StyledTab label={getLabel("explorer.tabs.articles")} value='toolbar.articles' />
        <StyledTab label={getLabel("explorer.tabs.services")} value='toolbar.services' />
        <StyledTab label={getLabel("explorer.tabs.links")} value='toolbar.links' />
      </StyledTabs>
      
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
  return (<Box sx={{ backgroundColor: "secondary.main", height: '100%' }}>{component}</Box>)
}
export { Secondary }


