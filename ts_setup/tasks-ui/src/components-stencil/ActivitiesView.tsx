import React from 'react';
import {
  Card, CardHeader, CardActions, CardContent,
  Typography, Box, Divider
} from '@mui/material';

import { FormattedMessage, useIntl } from 'react-intl';
import Burger from 'components-burger';

import { ArticleComposer } from './article';
import { LinkComposer } from './link';
import { WorkflowComposer } from './workflow';
import { LocaleComposer } from './locale';
import { ReleaseComposer } from './release';
import { NewPage } from './page';
import { MigrationComposer } from './migration';
import { TemplateComposer } from './template';

import { Composer, StencilClient } from './context';

import version from './version';
import { bullfighters_red, cocktail_green, cyan, purple_zergling, saffron, turquoise_topaz } from 'components-colors';

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

const createCards: (site: StencilClient.Site, tabs: Burger.TabsActions) => CardData[] = (_site, tabs) => ([
  {
    composer: (handleClose) => (<ArticleComposer onClose={handleClose} />),
    onView: () => tabs.handleTabAdd({ id: 'articles', label: "Articles" }),
    title: "activities.article.title",
    desc: "activities.article.desc",
    color: cyan,
    type: "article",
    buttonCreate: "article.create",
    buttonViewAll: undefined
  },
  {
    composer: (handleClose) => (<NewPage onClose={handleClose} />),
    onView: () => console.log("nothing to see here"),
    title: "activities.page.title",
    desc: "activities.page.desc",
    color: turquoise_topaz,
    type: "page",
    buttonCreate: "page.create",
    buttonViewAll: undefined
  },
  {
    composer: (handleClose) => (<LinkComposer onClose={handleClose} />),
    onView: () => tabs.handleTabAdd({ id: 'links', label: "Links" }),
    title: "activities.link.title",
    desc: "activities.link.desc",
    color: purple_zergling,
    type: "link",
    buttonCreate: "link.create",
    buttonViewAll: undefined
  },

  {
    composer: (handleClose) => (<WorkflowComposer onClose={handleClose} />),
    onView: () => tabs.handleTabAdd({ id: 'workflows', label: "Workflows" }),
    title: "services",
    desc: "services.desc",
    color: bullfighters_red,
    type: "workflow",
    buttonCreate: "services.create",
    buttonViewAll: undefined
  },

  {
    composer: (handleClose) => (<LocaleComposer onClose={handleClose} />),
    onView: () => tabs.handleTabAdd({ id: 'locales', label: "Locales" }),
    title: "activities.locale.title",
    desc: "activities.locale.desc",
    color: saffron,
    type: "locale",
    buttonCreate: "locale.create",
    buttonViewAll: "button.view.all.locales"
  },

  {
    composer: (handleClose) => (<ReleaseComposer onClose={handleClose} />),
    onView: () => tabs.handleTabAdd({ id: 'releases', label: "Releases" }),
    title: "activities.release.title",
    desc: "activities.release.desc",
    color: cocktail_green,
    type: "release",
    buttonCreate: "release.create",
    buttonViewAll: "button.view.all.releases",
    buttonTertiary: "button.releasegraph"
  },
  {
    composer: (handleClose) => <TemplateComposer onClose={handleClose}/>,
    onView: () => tabs.handleTabAdd({ id: 'templates', label: "Templates" }),
    title: "activities.templates.title",
    desc: "activities.templates.desc",
    color: cocktail_green,
    type: "templates",
    buttonCreate: "template.create",
    buttonViewAll: "button.view.all.templates"
  },
  {
    composer: (handleClose) => <MigrationComposer onClose={handleClose} />,
    onView: undefined,
    title: "activities.migration.title",
    desc: "activities.migration.desc",
    color: cocktail_green,
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
      <CardHeader sx={{ p: 1, backgroundColor: "table.main" }}
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
        <Typography color="mainContent.contrastText" variant="body2"><FormattedMessage id={props.data.desc} /></Typography>
      </CardContent>
      <Divider />
      
      <CardActions sx={{ alignSelf: "flex-end" }}>
        <Box display="flex">
          {props.data.buttonViewAll && props.data.onView ? <Burger.SecondaryButton onClick={props.data.onView} label={props.data.buttonViewAll} /> : <Box />}
          {props.data.buttonTertiary && props.data.onView ?
            <Burger.SecondaryButton label="button.releasegraph" onClick={() => tabs.actions.handleTabAdd({ id: 'graph', label: "Release Graph" })}
              sx={{
                color: cyan,
                alignSelf: 'center',
              }} /> : null}
          <Burger.PrimaryButton onClick={props.onCreate} label={props.data.buttonCreate} />
        </Box>
      </CardActions>
    </Card>
  )
}


//card view for all CREATE views
const ActivitiesView: React.FC<{}> = () => {
  const { actions } = Burger.useTabs();
  const { site } = Composer.useComposer();
  const { service } = Composer.useComposer();

  const [open, setOpen] = React.useState<number>();
  const handleClose = () => setOpen(undefined);
  const cards = React.useMemo(() => createCards(site, actions), [site, actions]);

  const [coreVersion, setCoreVersion] = React.useState<string>();
  const [coreVersionDate, setCoreVersionDate] = React.useState<string>();

  React.useEffect(() => {
    service.version().version().then((version) => {
      console.log(version);
      setCoreVersion(version.version);
      setCoreVersionDate(version.built);
    });    
  },  [service])


  return (
    <>
      <Typography variant="h3" fontWeight="bold" sx={{ p: 1, m: 1 }}><FormattedMessage id={"activities.title"} />
        <Typography variant="body2" sx={{pt: 1}}><FormattedMessage id={"activities.desc"} /></Typography>
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
          <FormattedMessage id={"activities.version.composer"} values={{ version: version.tag, date: version.built}}/>
          <Typography variant="caption" sx={{ pt: 1 }} >
            <FormattedMessage id={"activities.version.core"} values={{ version: coreVersion, date: coreVersionDate}}/>
          </Typography>
      </Typography>
    </>
  );
}

export { ActivitiesView }
