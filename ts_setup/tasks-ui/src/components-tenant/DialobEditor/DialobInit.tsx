import React from 'react';

import HTML5Backend from 'react-dnd-html5-backend';
import { DndProvider } from 'react-dnd';

import { combineReducers, applyMiddleware, createStore } from 'redux';
import { Provider } from 'react-redux';


import { DEFAULT_ITEM_CONFIG, DEFAULT_ITEMTYPE_CONFIG, DialobComposer, createDialobComposerReducer, createDialobComposerMiddleware } from 'components-dialob';
import './fixes.css'

const DialobInit: React.FC<{
  onClose: () => void,
  tenantId: string,
  formId: string,
  apiUrl: string,
  dialobOnly: boolean
}> = ({ tenantId, formId, apiUrl, dialobOnly, onClose }) => {

  const DIALOB_COMPOSER_CONFIG = {
    transport: {
      /*
      csrf: {
        headerName: undefined,
        token: undefined
      },
      */
      apiUrl: apiUrl,
      previewUrl: "",
      tenantId: tenantId,
      credentials: undefined// dialobOnly ? 'include' : undefined
    },
    documentationUrl: '',
    itemEditors: DEFAULT_ITEM_CONFIG,
    itemTypes: DEFAULT_ITEMTYPE_CONFIG,
    valueSetProps: undefined, //CUSTOM_VALUESET_PROPS,
    postAddItem: (_dispatch: any, _action: any, _lastItem: any) => { },
    closeHandler: () => {
      console.log("closing session");
      onClose();
    }
  };




  const reducers = {
    dialobComposer: createDialobComposerReducer()
  };
  const reducer = combineReducers(reducers);
  const store = createStore(reducer, applyMiddleware(...createDialobComposerMiddleware()));

  React.useEffect(() => {
    const styleElement = document.createElement('link');
    styleElement.rel = 'stylesheet';
    styleElement.href = '//cdn.jsdelivr.net/npm/semantic-ui@2.4.0/dist/semantic.min.css';
    document.head.append(styleElement);
    return () => styleElement.remove();
  }, []);

  //<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/semantic-ui@2.4.0/dist/semantic.min.css">


  return (<>
    ({/* @ts-ignore */}
    <DndProvider backend={HTML5Backend as any} key={formId} id={formId}>
      {/* @ts-ignore */}
      <Provider store={store} key={formId} id={formId}>
        <DialobComposer formId={formId} configuration={DIALOB_COMPOSER_CONFIG} />
      </Provider>
    </DndProvider>
  </>);
}



export { DialobInit };