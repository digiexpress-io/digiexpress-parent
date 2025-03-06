
import { Main } from './Main';
import { Secondary } from './Secondary';
import Toolbar from './Toolbar';
import HdesClient from './client';

import { Composer } from './context'; 
import version from './version';

const ComposerProvider = Composer.Provider;
const useComposer = Composer.useComposer;
const useBranchName = Composer.useBranchName;

export { Main, Secondary, Toolbar, ComposerProvider, useComposer, useBranchName, Composer, version };
export default HdesClient;