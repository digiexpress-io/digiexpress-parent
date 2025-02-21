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

      <Box sx={{ paddingBottom: 1, m: 2 }}>
        <Box display="flex">
          <Box alignSelf="center">
            <Typography variant="h3" sx={{ p: 1, mb: 3, fontWeight: "bold", color: "mainContent.dark" }}>
              <FormattedMessage id="releases" />: {releases.length}
              <Typography variant="body2" sx={{ pt: 1 }}><FormattedMessage id={"release.desc"} /></Typography>
            </Typography>
          </Box>
          <Box flexGrow={1} />
          <Box>
            <Button  onClick={() => layout.actions.handleTabCloseCurrent()} sx={{ marginRight: 1 }} variant='text'><FormattedMessage id='button.cancel'/></Button>
            <Button variant='text' onClick={() => layout.actions.handleTabAdd({ id: 'graph', label: "Release Graph" })} sx={{ marginRight: 1 }}>
              <FormattedMessage id="button.releasegraph"/>
            </Button>
            <Button variant='contained' onClick={() => setReleaseComposer(true)}>
              <FormattedMessage id="button.create"/>
            </Button>
          </Box>
        </Box>

        <Box display="flex" sx={{ justifyContent: 'center' }}>

          <Card sx={{ margin: 1, width: 'fill-available' }}>
            <Typography variant="h4" sx={{ p: 2, backgroundColor: "table.main" }}>
              <FormattedMessage id="releases" />
            </Typography>
            <ReleaseTable releases={releases} />
          </Card>
        </Box>
      </Box>
    </>
  );
}

export { ReleasesView }
