import React from 'react';
import { createFileRoute } from '@tanstack/react-router';
import { EveliSetup } from '../eveli-setup';
import { EveliTreeSetup } from '../../eveli-tree';
import { EveliApp } from '../eveli-app';
import { EveliTreeProvider } from '../../eveli-tree-api';

export const Route = createFileRoute('/secured/$locale/worker/fileexplorer/')({
  component: Component,
})

const MergedToolbar: React.FC = () => {
  return (<EveliSetup.Toolbar />);
};

function Component() {
  return (
    <EveliTreeProvider>
      <EveliApp
        main={EveliTreeSetup.Main}
        secondary={EveliTreeSetup.Secondary}
        toolbar={MergedToolbar}
        drawerWidth={450}
      />
    </EveliTreeProvider>
  );
}