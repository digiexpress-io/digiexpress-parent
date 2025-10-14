import React from 'react';

import { Card, CardHeader, CardActions, CardContent, Box, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { EveliActivities } from '@dxs-ts/eveli-primitives';

import { useTagomiNav } from '../tagomi-nav';
import { ServiceComposer } from '../tagomi-service';
import { LocaleComposer } from '../tagomi-locale';
import { NewTemplate } from '../tagomi-template';


export interface ActivityProps {
  title: React.ReactNode;
  desc: React.ReactNode;
  buttonCreate: React.ReactNode;
  buttonViewAll?: React.ReactNode;
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
          {props.data.buttonViewAll && props.data.onView ? <Button variant='text' onClick={props.data.onView} children={props.data.buttonViewAll} /> : <Box />}
          <Button onClick={handleOpen} children={props.data.buttonCreate} />
        </CardActions>
      </Card>
    </>
  )
}

export function useActivities(): ActivityProps[] {
  const nav = useTagomiNav();
  return ([

    {
      composer: ServiceComposer,
      onView: () => nav.onNav({ type: 'SERVICES' }),
      title: <FormattedMessage id="tagomi.services.title" />,
      desc: <FormattedMessage id="tagomi.services.desc" />,
      buttonCreate: <FormattedMessage id="tagomi.service.create" />,
      buttonViewAll: undefined
    },
    {
      composer: LocaleComposer,
      onView: () => nav.onNav({ type: 'LOCALES' }),
      title: <FormattedMessage id="tagomi.locales.title" />,
      desc: <FormattedMessage id="tagomi.locales.desc" />,
      buttonCreate: <FormattedMessage id="tagomi.locale.create" />,
      buttonViewAll: undefined,
    },
    {
      composer: NewTemplate,
      onView: () => nav.onNav({ type: 'TEMPLATES' }),
      title: <FormattedMessage id="tagomi.templates.title" />,
      desc: <FormattedMessage id="tagomi.templates.desc" />,
      buttonCreate: <FormattedMessage id="tagomi.template.create" />,
      buttonViewAll: undefined,
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
