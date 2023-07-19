//@ts-nocheck
import React from 'react';
import { Box } from '@mui/material';

import { Client, Composer } from '../context';

import HTML5Backend from 'react-dnd-html5-backend';
import { DndProvider } from 'react-dnd';

import { combineReducers, applyMiddleware, createStore } from 'redux';
import { Provider } from 'react-redux';
import {
  DialobComposer,
  createDialobComposerReducer, createDialobComposerMiddleware,
  DEFAULT_ITEM_CONFIG, DEFAULT_ITEMTYPE_CONFIG
} from './old-composer';



const ComposerInit: React.FC<{ formId: Client.FormId, config: Client.StoreConfig }> = ({ formId }) => {

  const DIALOB_COMPOSER_CONFIG = {
    transport: {
      csrf: {
        headerName: "",
        token: ""
      },
      apiUrl: "http://localhost:8081/assets",
      previewUrl: "",
      tenantId: "",
    },
    itemEditors: DEFAULT_ITEM_CONFIG,
    itemTypes: DEFAULT_ITEMTYPE_CONFIG,
    closeHandler: () => {
      console.log("close handler");
    }
  };

  const reducers = {
    dialobComposer: createDialobComposerReducer()
  };
  const reducer = combineReducers(reducers);
  const store = createStore(reducer, applyMiddleware(...createDialobComposerMiddleware()));
 
  return (<DndProvider backend={HTML5Backend as any} key={formId} id={formId}>
    <Provider store={store} key={formId} id={formId}>
      <DialobComposer formId={formId} configuration={DIALOB_COMPOSER_CONFIG} key={formId} id={formId}/>
    </Provider>
  </DndProvider >);
}

export { ComposerInit };
