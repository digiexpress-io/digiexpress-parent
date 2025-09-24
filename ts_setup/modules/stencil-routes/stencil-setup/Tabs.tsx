import React from 'react';

import { Tabs as MuiTabs, Tab, Stack, useTheme } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { ExplorerItem, useStencilNav, useStencilTabChange, useStencilTabClose, toExplorerId } from '../stencil-nav';
import { StencilComposerApi } from '@dxs-ts/stencil-api';
import { SaveOutlined } from '@mui/icons-material';
import { useIntl } from 'react-intl';




const ArticleTabIndicator: React.FC<{ item: ExplorerItem }> = ({ item }) => {
  const theme = useTheme();
  const { isArticleSaved, session } = StencilComposerApi.useComposer();

  if (item.type === 'ARTICLE_PAGES') {
    const view = session.articles.find(view => view.article.id === item.article);
    if (!view) {
      return (<></>)
    }
    const saved = isArticleSaved(view.article);
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
  const { session } = StencilComposerApi.useComposer();


  if (item.type === 'ARTICLE_LINKS' || item.type === 'ARTICLE_PAGES' || item.type === 'ARTICLE_WORKFLOWS') {
    const suffix = getLabelSuffix(item);
    function getLabelSuffix(item: ExplorerItem) {
      switch (item.type) {
        case 'ARTICLE_LINKS': return (intl.formatMessage({ id: 'explorer.tabs.links' }));
        case 'ARTICLE_PAGES': return (intl.formatMessage({ id: 'explorer.pages' }));
        case 'ARTICLE_WORKFLOWS': return (intl.formatMessage({ id: 'explorer.tabs.services' }));
      }
    }
    const view = session.articles.find(view => view.article.id === item.article);
    if (!view) {
      throw new Error(`Can't find article: ${item.article}`);
    }
    return <>{view.article.body.name}{intl.formatMessage({ id: 'eveli.textSeparatorColon' })}{suffix}</>;
  }

  if (item.type === 'ASSISTANCE') {
    return <>{intl.formatMessage({ id: 'article.timestamps.title' })}</>;
  }
  
  return (<>{item.type}</>)
}


export const Tabs: React.FC<{}> = () => {
  const { explorer, activeItem } = useStencilNav();
  const { onTabClose } = useStencilTabClose();
  const { onTabChange } = useStencilTabChange();

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