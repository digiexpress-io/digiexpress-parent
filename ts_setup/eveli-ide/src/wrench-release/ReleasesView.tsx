import React from 'react';

import { Typography, Button, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import ReleasesTable from './ReleasesTable';
import type { Release } from './release-types';
import { ReleaseBranch } from './release-types';
import { WrenchComposerApi as Composer } from '../wrench-setup';
import { useWrenchNav } from '../wrench-nav';


const ReleasesView: React.FC<{}> = () => {

  const { site } = Composer.useComposer();
  const { onTabCurrentClose, onNav } = useWrenchNav();
  const releases = Object.values(site.tags);
  const branches = Object.values(site.branches);

  const formattedReleases: Release[] = releases.map((release) => {
    const { id } = release;
    const name = release.ast?.name || '';
    const created = release.ast?.created || '';
    const note = release.ast?.description;
    const data = JSON.stringify(release.ast, null, 2);
    const releaseBranches: ReleaseBranch[] = branches.filter((branch) => branch.ast?.tagId === id).map((branch) => {
      return {
        id: branch.id,
        branch: branch.ast!,
      }
    });
    return {
      id,
      body: {
        name,
        note,
        created,
        data,
      },
      branches: releaseBranches
    }
  });

  return (
    <>
      <Box display="flex" sx={{ paddingBottom: 1 }}>
        <Box>
          <Typography variant="h1" sx={{ p: 1 }}>
            <FormattedMessage id="activities.releases.title" />: {releases.length}
          </Typography>
          <Typography variant="body2" sx={{ px: 1 }}>
            <FormattedMessage id="activities.releases.desc" />
          </Typography>
        </Box> 
        <Box flexGrow={1} />
        <Box alignSelf="center">
          <Button
            onClick={() => onTabCurrentClose()}
            sx={{ marginRight: 1 }}
            variant="text"
          >
            <FormattedMessage id="button.cancel" />
          </Button>
          <Button
            onClick={() => onNav({ type: 'COMPARE' })}
            variant="contained"
          >
            <FormattedMessage id="releases.button.compare" />
          </Button>
        </Box>
      </Box>
  
      <Typography variant="body2" sx={{ px: 1, marginBottom: 2 }}>
        <FormattedMessage id="activities.releases.desc.additional" />
      </Typography>
  
      <ReleasesTable releases={formattedReleases} />
    </>
  );  
}



export { ReleasesView }
