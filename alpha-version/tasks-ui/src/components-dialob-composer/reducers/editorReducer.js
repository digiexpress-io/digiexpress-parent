import Immutable, { List } from 'immutable';
import * as Actions from '../actions/constants';
import * as Status from '../helpers/constants';
import {findRoot} from '../helpers/utils';

const INITIAL_STATE = Immutable.Map();

function setErrors(state, errors, append) {
  let newState = append && state.get('errors') ? state.update('errors', e => e.concat(Immutable.fromJS(errors)))
                                               : state.set('errors', Immutable.fromJS(errors));

  if (errors && errors.length > 0) {
    if (errors.findIndex(e => e.level === 'FATAL') > -1) {
      return newState.set('status', Status.STATUS_FATAL);
    } else {
      if (errors.length === errors.filter(e => e.level === 'WARNING').length) {
        return newState.set('status', Status.STATUS_WARNINGS);
      } else {
        return newState.set('status', Status.STATUS_ERRORS);
      }
    }
  } else {
    return newState.set('status', Status.STATUS_OK);
  }
}

function clearErrors(state, prefix) {
  if (!state.get('errors')) {
    return state;
  } else {
    return state.update('errors', errors => errors.filter(e => prefix ? !e.get('message').startsWith(prefix) : false));
  }
}

function selectActiveLanguage(state, languages) {
  const activeLanguage = state.get('activeLanguage');
  if (!activeLanguage) {
    return languages[0];
  } else {
    if (languages.findIndex(l => l === activeLanguage) < 0) {
      return languages[0];
    } else {
      return activeLanguage;
    }
  }
}

export function editorReducer(state = INITIAL_STATE, action) {
  switch (action.type) {
    case Actions.SET_FORM:
      return state.delete('changeId')
                  .set('activeLanguage', selectActiveLanguage(state, action.formData.metadata.languages))
                  .set('rootItemId', findRoot(Immutable.fromJS(action.formData).get('data')).get('id'))
                  .set('loaded', true)
    case Actions.LOAD_FORM:
      return state.delete('loaded').delete('rootItemId');
    case Actions.SET_ACTIVE_ITEM:
      return state.set('activeItemId', action.itemId);
    case Actions.SET_ACTIVE_PAGE:
      return state.set('activeItemId', action.itemId).set('activePageId', action.itemId);
    case Actions.DELETE_ITEM:
      const nextState = state.get('activeItemId') === action.itemId ? state.delete('activeItemId') : state;
      return state.get('activePageId') === action.itemId ? nextState.delete('activePageId') : nextState;
    case Actions.SET_ACTIVE_LANGUAGE:
      return state.set('activeLanguage', action.language);
    case Actions.ASK_CONFIRMATION:
      return state.set('confirmableAction', action.action);
    case Actions.CANCEL_CONFIRMATION:
      return state.delete('confirmableAction');
    case Actions.SHOW_ITEM_OPTIONS:
      return state.set('itemOptions', Immutable.fromJS({itemId: action.itemId, isPage: action.isPage}));
    case Actions.HIDE_ITEM_OPTIONS:
      return state.delete('itemOptions');
    case Actions.SET_STATUS:
      return state.set('status', action.status);
    case Actions.SET_ERRORS:
      return setErrors(state, action.errors, action.append);
    case Actions.SHOW_FORM_OPTIONS:
      return state.set('formOptions', true);
    case Actions.HIDE_FORM_OPTIONS:
      return state.delete('formOptions');
    case Actions.SHOW_VARIABLES:
      return state.set('variablesDialog', true)
    case Actions.HIDE_VARIABLES:
      return state.delete('variablesDialog');
    case Actions.SHOW_CHANGE_ID:
      return state.set('changeId', action.changeId);
    case Actions.HIDE_CHANGE_ID:
      return state.delete('changeId');
    case Actions.SHOW_PREVIEW_CONTEXT:
      return state.set('previewContextDialog', true);
    case Actions.HIDE_PREVIEW_CONTEXT:
      return state.delete('previewContextDialog');
    case Actions.SHOW_VALUESETS:
      return state.set('valueSetsOpen', true);
    case Actions.HIDE_VALUESETS:
      return state.delete('valueSetsOpen');
    case Actions.SHOW_TRANSLATION:
      return state.set('translationOpen', true)
    case Actions.HIDE_TRANSLATION:
      return state.delete('translationOpen');
    case Actions.SHOW_VERSIONING:
      return state.set('versioningDialog', true);
    case Actions.HIDE_VERSIONING:
      return state.delete('versioningDialog').delete('versions');
    case Actions.FETCH_VERSIONS:
      return state.delete('versions');
    case Actions.SET_VERSIONS:
      return state.set('versions', Immutable.fromJS(action.versions));
    case Actions.SHOW_NEW_TAG:
      return state.set('newTagDialog', true);
    case Actions.HIDE_NEW_TAG:
      return clearErrors(state, 'TAG_').delete('newTagDialog');
    case Actions.PERFORM_CHANGE_ID:
      const newState = state.get('activeItemId') === action.oldId ? state.set('activeItemId', action.newId) : state;
      return newState.get('activePageId') === action.oldId ? newState.set('activePageId', action.newId) : newState;
    case Actions.SET_TREE_COLLAPSE:
      return state.update('treeCollapse', treeCollapse => {
        if (!treeCollapse) {
          treeCollapse = new List();
        }
        if (!action.collapsed && treeCollapse.findIndex(id => id === action.itemId) > -1) {
          treeCollapse = treeCollapse.delete(treeCollapse.findIndex(id => id === action.itemId))
        } else if (action.collapsed && treeCollapse.findIndex(id => id === action.itemId) === -1) {
          treeCollapse = treeCollapse.push(action.itemId);
        }
        return treeCollapse;
      });
    default:
      // NOP
  }
  return state;
}