import React from 'react';

import { Card, CardHeader, CardActions, CardContent, Box, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { EveliPermissions, EveliPermissionType, EveliActivities } from '@dxs-ts/eveli-primitives';

import { ArticleComposer } from '../stencil-article';
import { LinkComposer } from '../stencil-link';
import { WorkflowComposer } from '../stencil-workflow';
import { LocaleComposer } from '../stencil-locale';
import { NewPage } from '../stencil-page';
import { MigrationComposer } from '../stencil-migration';
import { TemplateComposer } from '../stencil-template';
import { useStencilNav } from '../stencil-nav';



export interface ActivityProps {
  title: React.ReactNode;
  desc: React.ReactNode;
  buttonCreate: React.ReactNode;
  buttonViewAll?: React.ReactNode;
  permissionTypeCreate: EveliPermissionType;
  permissionTypeView: EveliPermissionType;
  onView?: () => void;
  composer: React.FC<{ onClose: () => void }>;
}

const ActivitiesViewItem: React.FC<{ data: ActivityProps }> = (props) => {
  const [open, setOpen] = React.useState<boolean>(false);
  const handleClose = () => setOpen(false);
  const handleOpen = () => setOpen(true);

  const Composer: React.FC<{ onClose: () => void }> = open === false ? () => (<></>) : props.data.composer;

  return (
    <>
      <Composer onClose={handleClose} />
      <Card>
        <CardHeader title={props.data.title} />
        <CardContent>{props.data.desc}</CardContent>

        <CardActions>
          <EveliPermissions id={props.data.permissionTypeView}>
            {props.data.buttonViewAll && props.data.onView ? <Button variant='text' onClick={props.data.onView} children={props.data.buttonViewAll} /> : <Box />}
          </EveliPermissions>

          <EveliPermissions id={props.data.permissionTypeCreate}>
            <Button onClick={handleOpen} children={props.data.buttonCreate} />
          </EveliPermissions>
        </CardActions>
      </Card>
    </>
  )
}

export function useActivities(): ActivityProps[] {
  const nav = useStencilNav();
  return ([
    {
      composer: ArticleComposer,
      onView: () => nav.onNav({ type: 'ARTICLES' }),
      title: <FormattedMessage id="activities.article.title" />,
      desc: <FormattedMessage id="activities.article.desc" />,
      buttonCreate: <FormattedMessage id="article.create" />,
      buttonViewAll: undefined,
      permissionTypeCreate: 'CREATE_STENCIL_ASSET',
      permissionTypeView: 'NAV_TO_STENCIL_ARTICLES'
    },
    {
      composer: NewPage,
      onView: () => console.log("nothing to see here"),
      title: <FormattedMessage id="activities.page.title" />,
      desc: <FormattedMessage id="activities.page.desc" />,
      buttonCreate: <FormattedMessage id="page.create" />,
      buttonViewAll: undefined,
      permissionTypeCreate: 'CREATE_STENCIL_ASSET',
      permissionTypeView: 'NAV_TO_STENCIL_ARTICLES'
    },
    {
      composer: LinkComposer,
      onView: () => nav.onNav({ type: 'LINKS' }),
      title: <FormattedMessage id="activities.link.title" />,
      desc: <FormattedMessage id="activities.link.desc" />,
      buttonCreate: <FormattedMessage id="link.create" />,
      buttonViewAll: undefined,
      permissionTypeCreate: 'CREATE_STENCIL_ASSET',
      permissionTypeView: 'NAV_TO_STENCIL_LINKS'
    },

    {
      composer: WorkflowComposer,
      onView: () => nav.onNav({ type: 'SERVICES' }),
      title: <FormattedMessage id="services" />,
      desc: <FormattedMessage id="services.desc" />,
      buttonCreate: <FormattedMessage id="services.create" />,
      buttonViewAll: undefined,
      permissionTypeCreate: 'CREATE_STENCIL_ASSET',
      permissionTypeView: 'NAV_TO_STENCIL_SERVICES'
    },

    {
      composer: LocaleComposer,
      onView: () => nav.onNav({ type: 'LOCALES' }),
      title: <FormattedMessage id="activities.locale.title" />,
      desc: <FormattedMessage id="activities.locale.desc" />,
      buttonCreate: <FormattedMessage id="locale.create" />,
      buttonViewAll: <FormattedMessage id="button.view.all.locales" />,
      permissionTypeCreate: 'CREATE_STENCIL_ASSET',
      permissionTypeView: 'NAV_TO_STENCIL_LOCALES'
    },
    {
      composer: TemplateComposer,
      onView: () => nav.onNav({ type: 'TEMPLATES' }),
      title: <FormattedMessage id="activities.templates.title" />,
      desc: <FormattedMessage id="activities.templates.desc" />,
      buttonCreate: <FormattedMessage id="template.create" />,
      buttonViewAll: <FormattedMessage id="button.view.all.templates" />,
      permissionTypeCreate: 'CREATE_STENCIL_ASSET',
      permissionTypeView: 'NAV_TO_STENCIL_TEMPLATES'
    },
    {
      composer: MigrationComposer,
      onView: undefined,
      title: <FormattedMessage id="activities.migration.title" />,
      desc: <FormattedMessage id="activities.migration.desc" />,
      buttonCreate: <FormattedMessage id="migration.create" />,
      buttonViewAll: undefined,
      permissionTypeCreate: 'CREATE_STENCIL_ASSET',
      permissionTypeView: 'NAV_TO_STENCIL_MIGRATIONS'
    },
  ])
}


//card view for all CREATE views
export const Activities: React.FC<{}> = () => {
  const activities = useActivities();
  return (
    <EveliActivities>
      {activities.map((card, index) => (<ActivitiesViewItem key={index} data={card} />))}
    </EveliActivities>
  );
}
