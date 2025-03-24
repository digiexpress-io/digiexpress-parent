
import { Main } from './Main';
import { Tabs } from './Tabs';
import { Secondary } from './Secondary';
import Toolbar from './Toolbar';


import { Composer } from './context'; 
import version from './version';

const ComposerProvider = Composer.Provider;
const useComposer = Composer.useComposer;
const useBranchName = Composer.useBranchName;

export { Main, Secondary, Toolbar, Tabs, ComposerProvider, useComposer, useBranchName, Composer, version };