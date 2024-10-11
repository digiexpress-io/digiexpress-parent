import messages from './intl';

import { Main } from './Main';
import { Secondary } from './Secondary';
import Toolbar from './Toolbar';
import HdesClient from './client';

import { Composer } from './context'; 
import version from './version';

const ComposerProvider = Composer.Provider;
const useComposer = Composer.useComposer;


export { messages, Main, Secondary, Toolbar, ComposerProvider, useComposer, Composer, version };
export default HdesClient;