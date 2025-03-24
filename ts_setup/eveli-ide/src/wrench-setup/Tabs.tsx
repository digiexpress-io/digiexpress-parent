import React from 'react';

import { Tabs as MuiTabs, Tab, Box, Stack, useTheme } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { ExplorerItem, toExplorerId, useWrenchNav, useWrenchTabChange, useWrenchTabClose } from '../wrench-nav';
import { WrenchComposerApi } from './ide';



const ArticleTabIndicator: React.FC<{ item: ExplorerItem }> = ({ item }) => {
  const theme = useTheme();
  const { isArticleSaved, session } = WrenchComposerApi.useComposer();

  if(item.type === 'ENTITY_EDITOR') {
    const view = session.getEntity(item.id);
    if(!view) {
      return (<></>)
    }
    const saved = isArticleSaved(view);
    return <span style={{
      paddingLeft: "5px",
      fontSize: '30px',
      color: theme.palette.primary.main,
      display: saved ? "none" : undefined
    }}>*</span>
  }
  return (<></>)
}


export const Tabs: React.FC<{}> = () => {
  const { explorer, activeItem } = useWrenchNav();
  const { onTabClose } = useWrenchTabClose();
  const { onTabChange } = useWrenchTabChange();

  const handleTabClose = (_event: React.ChangeEvent<{}>, newValue: number) => {
    _event.stopPropagation();
    onTabClose(explorer[newValue]);
  }
  const handleTabChange = (_event: React.ChangeEvent<{}>, newValue: string) => {
    onTabChange(explorer.find(exp => toExplorerId(exp) === newValue));
  }

  if(explorer.length === 0 || !activeItem) {
    return (<></>)
  }

  return (
    <Stack spacing={1} direction='row'>
    <MuiTabs value={toExplorerId(activeItem)} onChange={handleTabChange} variant="scrollable" scrollButtons="auto" >
      {
        explorer.map((tab, index) => (
          <Tab key={index} 
            value={toExplorerId(tab)} wrapped={true}
            label={tab.type}
            iconPosition="end"
            icon={(<>
              <ArticleTabIndicator item={tab}/>
              <CloseIcon color="disabled" onClick={(e) => handleTabClose(e, index)}/>
              <Box component="span" sx={{ flexGrow: 1 }}></Box>
            </>)}
          />))
      }
    </MuiTabs>
  </Stack>
  )
}