import createDialobComposerReducer from './reducers';
import createDialobComposerMiddleware from './middleware';
import DialobComposer from './DialobComposer';
import {DEFAULT_ITEM_CONFIG, DEFAULT_ITEMTYPE_CONFIG, DEFAULT_VALUESET_PROPS} from './defaults';
import Item, {connectItem} from './items/Item';
import SimpleField from './items/SimpleField';
import Group from './items/Group';
import ItemMenu from './components/ItemMenu';
import * as DialobActions from './actions/editor';
import * as PropEditors from './components/propEditors';
import { MarkdownEditor } from './components/MarkdownEditor';

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
