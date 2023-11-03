import * as Actions from '../actions/constants';
import {setActivePage} from '../actions';

function findPageForItem(formData, rootItemId, itemId) {
  const pages = formData.getIn(['data', rootItemId, 'items']);
  const containsItem = (item, itemId) => {
    if (!item.get('items')) {
      return false;
    }
    for (let childId of item.get('items')) {
      if (childId === itemId) {
        return true;
      } else {
        const childItem = formData.getIn(['data', childId]);
        if (containsItem(childItem, itemId)) {
          return true;
        }
      }
    }
    return false;
  }

  if (pages.contains(itemId)) {
    return itemId; // Active item is a page
  }

  for (let pageId of pages) {
    const page = formData.getIn(['data', pageId]);
    if (containsItem(page, itemId)) {
      return pageId;
    }
  }

  return null;
}

export const navigationMiddleware = store => next => action => {
  if (action.type === Actions.SET_ACTIVE_ITEM) {
    const pageId = findPageForItem(store.getState().dialobComposer.form, store.getState().dialobComposer.editor.get('rootItemId'), action.itemId);
    store.dispatch(setActivePage(pageId));
  }
  return next(action);
}