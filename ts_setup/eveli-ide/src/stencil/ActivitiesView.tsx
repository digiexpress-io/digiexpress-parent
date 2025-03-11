import React from 'react';

import { Card, CardHeader, CardActions, CardContent, Typography, Box, Button } from '@mui/material';

import { FormattedMessage, useIntl } from 'react-intl';
import { ArticleComposer } from './article';
import { LinkComposer } from './link';
import { WorkflowComposer } from './workflow';
import { LocaleComposer } from './locale';
import { ReleaseComposer } from './release';
import { NewPage } from './page';
import { MigrationComposer } from './migration';
import { TemplateComposer } from './template';

import { Composer } from './context';

import composerVersion from './version';
import { useStencilNav } from './nav';

interface CardData {
  type: CardType;
  title: string;
  desc: string;
  open?: boolean;
  buttonCreate: string;
  buttonViewAll?: string;
  buttonTertiary?: string;
  onView?: () => void;
  composer: (handleClose: () => void) => React.ReactChild;
  //viewer: (() => void) => xxx;
}

type CardType = "release" | "article" | "page" | "link" | "workflow" | "locale" | "migration" | "templates";

const createCards: (tabs: ReturnType<typeof useStencilNav>) => CardData[] = (tabs) => ([
  {
    composer: (handleClose) => (<ArticleComposer onClose={handleClose} />),
    onView: () => tabs.onNav({ type: 'ARTICLES' }),
    title: "activities.article.title",
    desc: "activities.article.desc",
    type: "article",
    buttonCreate: "article.create",
    buttonViewAll: undefined
  },
  {
    composer: (handleClose) => (<NewPage onClose={handleClose} />),
    onView: () => console.log("nothing to see here"),
    title: "activities.page.title",
    desc: "activities.page.desc",
    type: "page",
    buttonCreate: "page.create",
    buttonViewAll: undefined
  },
  {
    composer: (handleClose) => (<LinkComposer onClose={handleClose} />),
    onView: () => tabs.onNav('LINKS'),
    title: "activities.link.title",
    desc: "activities.link.desc",
    type: "link",
    buttonCreate: "link.create",
    buttonViewAll: undefined
  },

  {
    composer: (handleClose) => (<WorkflowComposer onClose={handleClose} />),
    onView: () => tabs.onNav('SERVICES'),
    title: "services",
    desc: "services.desc",
    type: "workflow",
    buttonCreate: "services.create",
    buttonViewAll: undefined
  },

  {
    composer: (handleClose) => (<LocaleComposer onClose={handleClose} />),
    onView: () => tabs.onNav('LOCALES'),
    title: "activities.locale.title",
    desc: "activities.locale.desc",
    type: "locale",
    buttonCreate: "locale.create",
    buttonViewAll: "button.view.all.locales"
  },

  {
    composer: (handleClose) => (<ReleaseComposer onClose={handleClose} />),
    onView: () => tabs.onNav('RELEASES'),
    title: "activities.release.title",
    desc: "activities.release.desc",
    type: "release",
    buttonCreate: "release.create",
    buttonViewAll: "button.view.all.releases",
    buttonTertiary: "button.releasegraph"
  },
  {
    composer: (handleClose) => <TemplateComposer onClose={handleClose} />,
    onView: () => tabs.onNav('TEMPLATES'),
    title: "activities.templates.title",
    desc: "activities.templates.desc",
    type: "templates",
    buttonCreate: "template.create",
    buttonViewAll: "button.view.all.templates"
  },
  {
    composer: (handleClose) => <MigrationComposer onClose={handleClose} />,
    onView: undefined,
    title: "activities.migration.title",
    desc: "activities.migration.desc",
    type: "migration",
    buttonCreate: "migration.create",
    buttonViewAll: undefined
  },

]);

const ActivitiesViewItem: React.FC<{ data: CardData, onCreate: () => void }> = (props) => {
  const title = useIntl().formatMessage({ id: props.data.title })
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

      <CardActions sx={{ alignSelf: "flex-end" }}>
        <Box display="flex">
          {props.data.buttonViewAll && props.data.onView ? <Button variant='text' onClick={props.data.onView} children={<FormattedMessage id={props.data.buttonViewAll} />} /> : <Box />}
          <Button onClick={props.onCreate} children={<FormattedMessage id={props.data.buttonCreate} />}/>
        </Box>
      </CardActions>
    </Card>
  )
}


//card view for all CREATE views
const ActivitiesView: React.FC<{}> = () => {
  const nav = useStencilNav();
  const { service } = Composer.useComposer();

  const [open, setOpen] = React.useState<number>();
  const handleClose = () => setOpen(undefined);
  const cards = createCards(nav);

  const [coreVersion, setCoreVersion] = React.useState<{ version: string, built: string }>();

  React.useEffect(() => {
    service.version().then((version) => {
      setCoreVersion(version);
    });
  }, [service]);

  return (
    <>
      <Typography variant="h1" fontWeight="bold" sx={{ p: 1, m: 1 }}><FormattedMessage id={"activities.title"} />
        <Typography variant="body2"><FormattedMessage id={"activities.desc"} /></Typography>
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
