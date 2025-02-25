import React from 'react';
import { Box, Typography, Card, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';


import * as Burger from '@/burger';
import { Composer } from '../context';
import { ReleaseComposer } from './';
import { ReleaseTable } from './ReleaseTable';


const ReleasesView: React.FC<{}> = () => {
  const { site } = Composer.useComposer();
  const layout = Burger.useTabs();
  const releases = Object.values(site.releases);
  const [releaseComposer, setReleaseComposer] = React.useState(false);

  return (
    <>
      {releaseComposer ? <ReleaseComposer onClose={() => setReleaseComposer(false)} /> : null}
      <Typography variant='h3'>
        <FormattedMessage id="releases" />: {releases.length}
      </Typography>
      <Typography variant="body2"><FormattedMessage id={"release.desc"} /></Typography>
      
      <Button onClick={() => layout.actions.handleTabCloseCurrent()} variant='text'><FormattedMessage id='button.cancel'/></Button>
      <Button variant='text' onClick={() => layout.actions.handleTabAdd({ id: 'graph', label: "Release Graph" })}>
        <FormattedMessage id="button.releasegraph"/>
      </Button>
      <Button variant='contained' onClick={() => setReleaseComposer(true)}>
        <FormattedMessage id="button.create"/>
      </Button>
      <ReleaseTable releases={releases} />
    </>
  );
}

export { ReleasesView }
