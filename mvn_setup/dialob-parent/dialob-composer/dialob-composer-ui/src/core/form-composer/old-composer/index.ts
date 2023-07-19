import createDialobComposerReducer from './js/reducers';
import createDialobComposerMiddleware from './js/middleware';
import DialobComposer from './js/DialobComposer';
import {DEFAULT_ITEM_CONFIG, DEFAULT_ITEMTYPE_CONFIG, DEFAULT_VALUESET_PROPS} from './js/defaults';
import Item, {connectItem} from './js/items/Item';
import SimpleField from './js/items/SimpleField';
import Group from './js/items/Group';
import ItemMenu from './js/components/ItemMenu';
import * as DialobActions from './js/actions/editor';
import * as PropEditors from './js/components/propEditors/';
import { MarkdownEditor } from './js/components/MarkdownEditor';
import { Config } from './dialob-client/FormService';

export type {
  Config
};


export {
  DialobComposer,
  createDialobComposerReducer,
  createDialobComposerMiddleware,
  DEFAULT_ITEM_CONFIG,
  DEFAULT_ITEMTYPE_CONFIG,
  DEFAULT_VALUESET_PROPS,
  Item,
  SimpleField,
  Group,
  connectItem,
  ItemMenu,
  DialobActions,
  PropEditors,
  MarkdownEditor
};
