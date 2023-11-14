import React from 'react';
import { Dialog } from '@mui/material';

import { TenantEntryDescriptor } from 'descriptor-tenant';
import Context from 'context';
import { DialobForm } from 'client';

import HTML5Backend from 'react-dnd-html5-backend';
import { DndProvider } from 'react-dnd';

import { combineReducers, applyMiddleware, createStore } from 'redux';
import { Provider } from 'react-redux';


import { DEFAULT_ITEM_CONFIG, DEFAULT_ITEMTYPE_CONFIG, DialobComposer, createDialobComposerReducer, createDialobComposerMiddleware } from 'components-dialob';


const DialobEditor: React.FC<{
  onClose: () => void,
  entry: TenantEntryDescriptor,
  form: DialobForm | undefined,

}> = ({ form, entry }) => {

  const backend = Context.useBackend();

  if (!form) {
    return null;
  }
  const apiUrl: string = backend.config.urls.find(url => url.id === 'dialob')?.url ?? '';

  const DIALOB_COMPOSER_CONFIG = {
    transport: {
      /*
      csrf: {
        headerName: undefined,
        token: undefined
      },
      */
      apiUrl: apiUrl + "api",
      previewUrl: "",
      tenantId: entry.tenantId,
    },
    documentationUrl: '',
    itemEditors: DEFAULT_ITEM_CONFIG,
    itemTypes: DEFAULT_ITEMTYPE_CONFIG,
    valueSetProps: undefined, //CUSTOM_VALUESET_PROPS,
    postAddItem: (_dispatch: any, _action: any, _lastItem: any) => { },
    closeHandler: () => { }
  };


  const reducers = {
    dialobComposer: createDialobComposerReducer()
  };
  const reducer = combineReducers(reducers);
  const store = createStore(reducer, applyMiddleware(...createDialobComposerMiddleware()));

  const formId: string = form._id;
  const DialobCss = React.lazy(() => import('./DialobCss'));

  return (<Dialog open={true} fullScreen>

    <React.Suspense fallback={<>loading composer</>}>
      <DialobCss /> &&
      ({/* @ts-ignore */}
      <DndProvider backend={HTML5Backend as any} key={formId} id={formId}>
        {/* @ts-ignore */}
        <Provider store={store} key={formId} id={formId}>
          <DialobComposer formId={formId} configuration={DIALOB_COMPOSER_CONFIG} />
        </Provider>
      </DndProvider>)
    </React.Suspense>
  </Dialog>);
}



export { DialobEditor };