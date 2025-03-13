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

import { useStencilNav } from './nav';
import * as Burger from '@/burger'

export interface ActivityProps {
  title: React.ReactNode;
  desc: React.ReactNode;

  buttonCreate: React.ReactNode;
  buttonViewAll?: React.ReactNode;

  onView?: () => void;
  composer: React.FC< {onClose: () => void}>;
}

const ActivitiesViewItem: React.FC<{ data: ActivityProps }> = (props) => {
  const [open, setOpen] = React.useState<boolean>(false);
  const handleClose = () => setOpen(false);
  const handleOpen = () => setOpen(true);

  const Composer: React.FC< {onClose: () => void}> = open === false ? () => (<></>) : props.data.composer;

  return (
    <>
    <Composer onClose={handleClose}/>
    <Card>
      <CardHeader title={props.data.title} />
      <CardContent>{props.data.desc}</CardContent>
      <CardActions>
        {props.data.buttonViewAll && props.data.onView ? <Button variant='text' onClick={props.data.onView} children={props.data.buttonViewAll} /> : <Box />}
        <Button onClick={handleOpen} children={props.data.buttonCreate}/>
      </CardActions>
    </Card>
    </>
  )
}

export function useActivities() {
  const nav = useStencilNav();
  return ([
    {
      composer: ArticleComposer,
      onView: () => nav.onNav({ type: 'ARTICLES' }),
      title: <FormattedMessage id="activities.article.title"/>,
      desc: <FormattedMessage id="activities.article.desc"/>,
      buttonCreate: <FormattedMessage id="article.create"/>,
      buttonViewAll: undefined
    },
    {
      composer: NewPage,
      onView: () => console.log("nothing to see here"),
      title: <FormattedMessage id="activities.page.title"/>,
      desc: <FormattedMessage id="activities.page.desc"/>,
  
      buttonCreate: <FormattedMessage id="page.create"/>,
      buttonViewAll: undefined
    },
    {
      composer: LinkComposer,
      onView: () => nav.onNav({type: 'LINKS'}),
      title: <FormattedMessage id="activities.link.title"/>,
      desc: <FormattedMessage id="activities.link.desc"/>,
      buttonCreate: <FormattedMessage id="link.create"/>,
      buttonViewAll: undefined
    },
  
    {
      composer: WorkflowComposer,
      onView: () => nav.onNav({type: 'SERVICES'}),
      title: <FormattedMessage id="services"/>,
      desc: <FormattedMessage id="services.desc"/>,
      buttonCreate: <FormattedMessage id="services.create"/>,
      buttonViewAll: undefined
    },
  
    {
      composer: LocaleComposer,
      onView: () => nav.onNav({type: 'LOCALES'}),
      title: <FormattedMessage id="activities.locale.title"/>,
      desc: <FormattedMessage id="activities.locale.desc"/>,
      buttonCreate: <FormattedMessage id="locale.create"/>,
      buttonViewAll: <FormattedMessage id="button.view.all.locales"/>,
    },
  
    {
      composer: ReleaseComposer,
      onView: () => nav.onNav({type: 'RELEASES'}),
      title: <FormattedMessage id="activities.release.title"/>,
      desc: <FormattedMessage id="activities.release.desc"/>,
      buttonCreate: <FormattedMessage id="release.create"/>,
      buttonViewAll: <FormattedMessage id="button.view.all.releases"/>,
    },
    {
      composer: TemplateComposer,
      onView: () => nav.onNav({type: 'TEMPLATES'}),
      title: <FormattedMessage id="activities.templates.title"/>,
      desc: <FormattedMessage id="activities.templates.desc"/>,
      buttonCreate: <FormattedMessage id="template.create"/>,
      buttonViewAll: <FormattedMessage id="button.view.all.templates"/>,
    },
    {
      composer: MigrationComposer,
      onView: undefined,
      title: <FormattedMessage id="activities.migration.title"/>,
      desc: <FormattedMessage id="activities.migration.desc"/>,
      buttonCreate: <FormattedMessage id="migration.create"/>,
      buttonViewAll: undefined
    },
  ])
}


//card view for all CREATE views
export const Activities: React.FC<{}> = () => {
  const activities = useActivities();
  return (
    <Burger.EveliActivities>
      {activities.map((card, index) => (<ActivitiesViewItem key={index} data={card} />))}
    </Burger.EveliActivities>
  );
}
