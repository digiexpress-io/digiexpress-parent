import React from 'react';
import { createFileRoute } from '@tanstack/react-router';
import { EveliSetup } from '../eveli-setup';
import { FsSetup } from '@dxs-ts/fs-composer';
import { EveliApp } from '../eveli-app';
import { FsNavProvider } from '@dxs-ts/fs-api';

export const Route = createFileRoute('/secured/$locale/worker/filesystem/')({
  component: Component,
})

const MergedToolbar: React.FC = () => {
  return (<EveliSetup.Toolbar />);
};

function Component() {
  return (
    <FsNavProvider>
      <EveliApp
        main={FsSetup.Main}
        secondary={FsSetup.Secondary}
        toolbar={MergedToolbar}
        drawerWidth={450}
      />
    </FsNavProvider>
  );
}