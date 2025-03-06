import React from 'react';

import { Typography, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import ReleasesTable from './ReleasesTable';
import type { Release } from './release-types';
import { ReleaseBranch } from './release-types';
import { Composer } from '../context';
import * as Burger from '@/burger';


const ReleasesView: React.FC<{}> = () => {

  const { site } = Composer.useComposer();
  const layout = Burger.useTabs();
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
      <Typography variant="h3">
        <FormattedMessage id="activities.releases.title" />: {releases.length}
      </Typography>

      <Typography variant="body2"><FormattedMessage id={"activities.releases.desc"} /></Typography>
      <Typography variant="body2"><FormattedMessage id={"activities.releases.desc.additional"} /></Typography>

      <Button  onClick={() => layout.handleTabCloseCurrent()} variant='text'><FormattedMessage id='button.cancel'/></Button>
      <Button  onClick={() => layout.handleTabAdd({ id: 'graph', label: "Release Graph" })} variant='text'><FormattedMessage id='activities.releases.graph'/></Button>
      <Button  onClick={() => layout.handleTabAdd({ id: 'compare', label: "Compare" })}  variant='text'><FormattedMessage id='releases.button.compare'/></Button>

      <ReleasesTable releases={formattedReleases} />
    </>
  );
}



export { ReleasesView }
