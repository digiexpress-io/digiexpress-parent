import React from 'react';
import { Typography, Button, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { StencilComposerApi as Composer } from '@/stencil-setup';
import { ReleaseComposer } from '.';
import { ReleaseTable } from './ReleaseTable';
import { useStencilNav } from '../stencil-nav';
import { CancelButton } from '@/eveli-styles';


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
        <Box display="flex" gap={1}>
          <CancelButton onClick={() => onTabCurrentClose()} />
          <Button variant='contained' onClick={() => setReleaseComposer(true)}>
            <FormattedMessage id="button.create" />
          </Button>
        </Box>
      </Box>

      <ReleaseTable releases={releases} />
    </>
  );
}

export { ReleasesView }
