import React from 'react';

import { Tabs as MuiTabs, Tab, Stack, useTheme } from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { ExplorerItem, useTagomiNav, useTagomiTabChange, useTagomiTabClose, toExplorerId } from '../tagomi-nav';
import { TagomiComposerApi } from '@dxs-ts/tagomi-api';
import { SaveOutlined } from '@mui/icons-material';
import { useIntl } from 'react-intl';




const ArticleTabIndicator: React.FC<{ item: ExplorerItem }> = ({ item }) => {
  const theme = useTheme();
  const { isServiceSaved, session } = TagomiComposerApi.useComposer();

  if (item.type === 'SERVICE_TEMPLATES') {
    const view = session.services.find(view => view.service.id === item.article);
    if (!view) {
      return (<></>)
    }
    const saved = isServiceSaved(view.service);
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
  const intl = useIntl();
  const { session } = TagomiComposerApi.useComposer();

  if (item.type === 'SERVICE_TEMPLATES') {
    return intl.formatMessage({ id: 'tagomi.templates.templateTab' })
  }
  return (<>{item.type}</>)
}


export const Tabs: React.FC<{}> = () => {
  const { explorer, activeItem } = useTagomiNav();
  const { onTabClose } = useTagomiTabClose();
  const { onTabChange } = useTagomiTabChange();

  const handleTabClose = (_event: React.ChangeEvent<{}>, newValue: number) => {
    _event.stopPropagation();
    onTabClose(explorer[newValue]);
  }
  const handleTabChange = (_event: React.ChangeEvent<{}>, newValue: string) => {
    onTabChange(explorer.find(exp => toExplorerId(exp) === newValue));
  }

  if (!activeItem) {
    return (<></>)
  }

  return (
    <Stack spacing={1} direction='row'>
      <MuiTabs value={toExplorerId(activeItem)} onChange={handleTabChange} variant="scrollable" scrollButtons="auto" >
        {
          explorer.map((tab, index) => (
            <Tab key={index}
              value={toExplorerId(tab)} wrapped={true}
              label={<TabLabel item={tab} />}
              iconPosition="end"
              icon={(<>
                <ArticleTabIndicator item={tab} />
                <CloseIcon color="disabled" onClick={(e) => handleTabClose(e, index)} />
              </>)}
            />))
        }
      </MuiTabs>
    </Stack>
  )
}