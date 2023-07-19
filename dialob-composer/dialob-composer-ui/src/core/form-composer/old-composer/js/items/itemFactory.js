import React from 'react';
import {DEFAULT_ITEM_CONFIG} from '../defaults';
import TreeItem from './TreeItem';

function itemFactory(item, props = {}, config = DEFAULT_ITEM_CONFIG) {
  if (!item) {
    return null;
  }
  let itemConfig = config.items.find(c => c.matcher(item, props));
  if (!itemConfig) {
    console.warn('Unknown type:', item.get('type'));
  }
  return itemConfig ? <itemConfig.component key={item.get('id')} itemType={item.get('type')} itemId={item.get('id')} {...itemConfig.props} {...props} /> : null;
}

function treeItemFactory(item, props = {}, config = DEFAULT_ITEM_CONFIG) {
  if (!item) {
    return null;
  }
  let itemConfig = config.items.find(c => c.matcher(item, props));
  if (itemConfig && itemConfig.hideInTree) {
    return null;
  }
  return itemConfig ? <TreeItem key={item.get('id')} id={item.get('id')} itemType={item.get('type')} itemId={item.get('id')} {...itemConfig.props} {...props} /> : null;
}

export {
  itemFactory,
  treeItemFactory
};
