import React from 'react';

import {
  Card, CardHeader, CardActions, CardContent, Theme, useTheme,
  Typography, Box, Divider, Button
} from '@mui/material';

import { FormattedMessage, useIntl } from 'react-intl';
import * as Burger from '@/burger';
import { BurgerApi } from '@/burger';
import { ArticleComposer } from './article';
import { LinkComposer } from './link';
import { WorkflowComposer } from './workflow';
import { LocaleComposer } from './locale';
import { ReleaseComposer } from './release';
import { NewPage } from './page';
import { MigrationComposer } from './migration';
import { TemplateComposer } from './template';

import { Composer, StencilApi } from './context';

import composerVersion from './version';

interface CardData {
  type: CardType;
  title: string;
  desc: string;
  buttonCreate: string;
  buttonViewAll?: string;
  buttonTertiary?: string;
  color: string;
  onView?: () => void;
  composer: (handleClose: () => void) => React.ReactChild;
  //viewer: (() => void) => xxx;
}

type CardType = "release" | "article" | "page" | "link" | "workflow" | "locale" | "migration" | "templates";

const createCards: (site: StencilApi.Site, theme: Theme, tabs: BurgerApi.TabsActions) => CardData[] = (_site, theme, tabs) => ([
  {
    composer: (handleClose) => (<ArticleComposer onClose={handleClose} />),
    onView: () => tabs.handleTabAdd({ id: 'articles', label: "Articles" }),
    title: "activities.article.title",
    desc: "activities.article.desc",
    color: theme.palette.primary.main,
    type: "article",
    buttonCreate: "article.create",
    buttonViewAll: undefined
  },
  {
    composer: (handleClose) => (<NewPage onClose={handleClose} />),
    onView: () => console.log("nothing to see here"),
    title: "activities.page.title",
    desc: "activities.page.desc",
    color: theme.palette.secondary.contrastText,
    type: "page",
    buttonCreate: "page.create",
    buttonViewAll: undefined
  },
  {
    composer: (handleClose) => (<LinkComposer onClose={handleClose} />),
    onView: () => tabs.handleTabAdd({ id: 'links', label: "Links" }),
    title: "activities.link.title",
    desc: "activities.link.desc",
    color: Burger.colors.purple,
    type: "link",
    buttonCreate: "link.create",
    buttonViewAll: undefined
  },

  {
    composer: (handleClose) => (<WorkflowComposer onClose={handleClose} />),
    onView: () => tabs.handleTabAdd({ id: 'workflows', label: "Workflows" }),
    title: "services",
    desc: "services.desc",
    color: Burger.colors.red,
    type: "workflow",
    buttonCreate: "services.create",
    buttonViewAll: undefined
  },

  {
    composer: (handleClose) => (<LocaleComposer onClose={handleClose} />),
    onView: () => tabs.handleTabAdd({ id: 'locales', label: "Locales" }),
    title: "activities.locale.title",
    desc: "activities.locale.desc",
    color: theme.palette.secondary.light,
    type: "locale",
    buttonCreate: "locale.create",
    buttonViewAll: "button.view.all.locales"
  },

  {
    composer: (handleClose) => (<ReleaseComposer onClose={handleClose} />),
    onView: () => tabs.handleTabAdd({ id: 'releases', label: "Releases" }),
    title: "activities.release.title",
    desc: "activities.release.desc",
    color: theme.palette.secondary.contrastText,
    type: "release",
    buttonCreate: "release.create",
    buttonViewAll: "button.view.all.releases",
    buttonTertiary: "button.releasegraph"
  },
  {
    composer: (handleClose) => <TemplateComposer onClose={handleClose} />,
    onView: () => tabs.handleTabAdd({ id: 'templates', label: "Templates" }),
    title: "activities.templates.title",
    desc: "activities.templates.desc",
    color: theme.palette.secondary.contrastText,
    type: "templates",
    buttonCreate: "template.create",
    buttonViewAll: "button.view.all.templates"
  },
  {
    composer: (handleClose) => <MigrationComposer onClose={handleClose} />,
    onView: undefined,
    title: "activities.migration.title",
    desc: "activities.migration.desc",
    color: theme.palette.secondary.contrastText,
    type: "migration",
    buttonCreate: "migration.create",
    buttonViewAll: undefined
  },

]);

const ActivitiesViewItem: React.FC<{ data: CardData, onCreate: () => void }> = (props) => {
  const title = useIntl().formatMessage({ id: props.data.title })
  const tabs = Burger.useTabs();
  return (

    <Card sx={{
      margin: 3,
      width: '20vw',
      display: 'flex',
      flexDirection: 'column',
    }}>
      <CardHeader
        title={
          <Box display="flex"
            sx={{
              justifyContent: 'center',
            }}>
            <Typography variant="h2" sx={{ fontWeight: 'bold', p: 1 }}>{title}</Typography>
          </Box>
        }
      />

      <CardContent sx={{ flexGrow: 1, p: 2, height: 'fit-content' }}>
        <Typography variant="body2"><FormattedMessage id={props.data.desc} /></Typography>
      </CardContent>
      <Divider />

      <CardActions sx={{ alignSelf: "flex-end" }}>
        <Box display="flex">
          {props.data.buttonViewAll && props.data.onView ? <Button variant='text' onClick={props.data.onView} children={<FormattedMessage id={props.data.buttonViewAll} />} /> : <Box />}
          {props.data.buttonTertiary && props.data.onView ?
            <Button variant='text' onClick={() => tabs.actions.handleTabAdd({ id: 'graph', label: "Release Graph" })}
              children={<FormattedMessage id='button.releasegraph' />}/> : null}
          <Button onClick={props.onCreate} children={<FormattedMessage id={props.data.buttonCreate} />}/>
        </Box>
      </CardActions>
    </Card>
  )
}


//card view for all CREATE views
const ActivitiesView: React.FC<{}> = () => {
  const theme = useTheme();
  const { actions } = Burger.useTabs();
  const { site } = Composer.useComposer();
  const { service } = Composer.useComposer();

  const [open, setOpen] = React.useState<number>();
  const handleClose = () => setOpen(undefined);
  const cards = React.useMemo(() => createCards(site, theme, actions), [site, theme, actions]);

  const [coreVersion, setCoreVersion] = React.useState<{ version: string, built: string }>();

  React.useEffect(() => {
    service.version().then((version) => {
      console.log("core version", version, "composer version", composerVersion);
      setCoreVersion(version);
    });
  }, [service]);

  return (
    <>
      <Typography variant="h3" fontWeight="bold" sx={{ p: 1, m: 1 }}><FormattedMessage id={"activities.title"} />
        <Typography variant="body2" sx={{ pt: 1 }}><FormattedMessage id={"activities.desc"} /></Typography>
      </Typography>
      <Box sx={{
        margin: 1,
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'center',

      }}>
        {open === undefined ? null : (cards[open].composer(handleClose))}
        {cards.map((card, index) => (<ActivitiesViewItem key={index} data={card} onCreate={() => setOpen(index)} />))}
      </Box>
      <Typography variant="caption" sx={{ pt: 1 }} display={'flex'} flexDirection={'column'} alignItems={'center'}>
        <FormattedMessage id={"activities.version.composer"} values={{ version: composerVersion.tag, date: composerVersion.built }} />
        <Typography variant="caption" sx={{ pt: 1 }} >
          <FormattedMessage id={"activities.version.core"} values={{ version: coreVersion?.version, date: coreVersion?.built }} />
        </Typography>
      </Typography>
    </>
  );
}

export { ActivitiesView }
