import React from 'react';

import { Tabs as MuiTabs, Tab, Box, Stack, useTheme } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { ExplorerItem, toExplorerId, useWrenchNav, useWrenchTabChange, useWrenchTabClose } from '../wrench-nav';
import { WrenchComposerApi } from '@dxs-ts/wrench-api';
import { SaveOutlined } from '@mui/icons-material';



const ArticleTabIndicator: React.FC<{ item: ExplorerItem }> = ({ item }) => {
  const theme = useTheme();
  const { isArticleSaved, session } = WrenchComposerApi.useComposer();

  if (item.type === 'ENTITY_EDITOR') {
    const view = session.getEntity(item.id);
    if (!view) {
      return (<></>)
    }
    const saved = isArticleSaved(view);
    return (
      <SaveOutlined sx={saved ? { display: 'none' } : {
        display: 'inherit',
        '&.MuiSvgIcon-root': { color: theme.palette.warning.main },
      }}
      />)
  }
  return (<></>)
}

const TabLabel: React.FC<{ item: ExplorerItem }> = ({ item }) => {
  const { session } = WrenchComposerApi.useComposer();

  if (item.type === 'ENTITY_EDITOR') {
    const view = session.getEntity(item.id);
    return view?.ast?.name ?? item.type;
  }

  return (<>{item.type}</>)
}

export const Tabs: React.FC<{}> = () => {
  const { explorer, activeItem } = useWrenchNav();
  const { onTabChange } = useWrenchTabChange();
  const { onTabClose } = useWrenchTabClose();

  const handleTabChange = (_event: React.ChangeEvent<{}>, newValue: string) => {
    onTabChange(explorer.find(exp => toExplorerId(exp) === newValue));
  }
  const handleTabClose = (_event: React.ChangeEvent<{}>, newValue: number) => {
    _event.stopPropagation();
    onTabClose(explorer[newValue]);
  }

  if (explorer.length === 0 || !activeItem) {
    return (<></>)
  }

  return (
    <Stack spacing={1} direction='row'>
      <MuiTabs value={toExplorerId(activeItem)} onChange={handleTabChange} variant="scrollable" scrollButtons="auto" >
        {explorer.map((tab, index) => <Tab
          value={toExplorerId(tab)}
          key={index}
          wrapped={true}
          label={<TabLabel item={tab} />}
          iconPosition="end"
          icon={(<>
            <ArticleTabIndicator item={tab} />
            <CloseIcon color="disabled" onClick={(e) => handleTabClose(e, index)} />
            <Box component="span" sx={{ flexGrow: 1 }}></Box>
          </>)}
        />)}
      </MuiTabs>
    </Stack>
  )
}