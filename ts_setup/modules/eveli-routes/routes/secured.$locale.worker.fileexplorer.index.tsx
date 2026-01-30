import React from 'react';
import { createFileRoute } from '@tanstack/react-router';
import { Secondary } from '../../eveli-tree/eveli-tree-setup/Secondary';
import { EveliSetup } from '../eveli-setup';
import { EveliApp } from '../eveli-app';

export const Route = createFileRoute('/secured/$locale/worker/fileexplorer/')({
  component: Component,
})

const MergedToolbar: React.FC = () => {
  return (
    <>
      <EveliSetup.Toolbar />
    </>
  );
};

function Component() {
  return (
    <EveliApp
      main={() => <></>}
      secondary={Secondary}
      toolbar={MergedToolbar}
    />
  );
}