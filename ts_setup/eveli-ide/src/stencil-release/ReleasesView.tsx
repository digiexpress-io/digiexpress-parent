import React from 'react';
import { Typography, Button, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { StencilComposerApi as Composer } from '../stencil-setup';
import { ReleaseComposer } from '.';
import { ReleaseTable } from './ReleaseTable';
import { useStencilNav } from '../stencil-nav';


const ReleasesView: React.FC<{}> = () => {
  const { site } = Composer.useComposer();
  const { onTabCurrentClose } = useStencilNav();


  const releases = Object.values(site.releases);
  const [releaseComposer, setReleaseComposer] = React.useState(false);

  return (
    <>
      {releaseComposer ? <ReleaseComposer onClose={() => setReleaseComposer(false)} /> : null}
      <Typography variant='h1'><FormattedMessage id="releases" />: {releases.length}</Typography>

      <Box display='flex' alignItems='center' my={1}>
        <Box maxWidth='75%' flexWrap='wrap'>
          <Typography variant="body2"><FormattedMessage id="release.desc" /></Typography>
        </Box>
        <Box flexGrow={1} />
        <Button onClick={() => onTabCurrentClose()} variant='text'><FormattedMessage id='button.cancel' /></Button>
        <Button variant='contained' onClick={() => setReleaseComposer(true)}>
          <FormattedMessage id="button.create" />
        </Button>
      </Box>

      <ReleaseTable releases={releases} />
    </>
  );
}

export { ReleasesView }
