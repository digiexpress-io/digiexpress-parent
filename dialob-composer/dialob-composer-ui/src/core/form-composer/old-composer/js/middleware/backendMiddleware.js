import * as Actions from '../actions/constants';
import * as Status from '../helpers/constants';
import FormService from '../../dialob-client/FormServiceImpl';
import {setForm, saveForm, setFormRevision, setStatus, setErrors, redirectPreview, setVersions, hideNewTag, scheduleSave} from '../actions';

const SAVE_DELAY = 500;
var saveTimer;

export const backendMiddleware = store => {
  return next => action => {
    let formService = null;
    if (store.getState().dialobComposer.config.transport) {
      let config = store.getState().dialobComposer.config.transport;

      formService = new FormService(config.apiUrl, config.csrf, config.tenantId);
    } else {
      return next(action);
    }

    if (action.type === Actions.LOAD_FORM) {
      formService.loadForm(action.formId, action.tagName)
        .then(json => {
          store.dispatch(setForm(json, action.tagName));
          store.dispatch(setStatus(Status.STATUS_OK));
          store.dispatch(saveForm(true));
        })
        .catch(error => store.dispatch(setErrors([{severity: 'FATAL', message: error.message}])));
    } else if (action.type === Actions.SAVE_FORM) {
      if (!store.getState().dialobComposer.form.get('_tag')) {
        if (store.getState().dialobComposer.editor.get('status') === Status.STATUS_BUSY) {
          store.dispatch(scheduleSave());
        } else {
          store.dispatch(setStatus(Status.STATUS_BUSY));
          formService.saveForm(store.getState().dialobComposer.form.toJS(), action.dryRun)
            .then(json => {
              store.dispatch(setFormRevision(json.rev));
              store.dispatch(setErrors(json.errors));
            })
            .catch(error => store.dispatch(setErrors([{severity: 'FATAL', message: error.message}])));
        }
      }
    } else if (action.type === Actions.PERFORM_CHANGE_ID) {
      formService.changeItemId(store.getState().dialobComposer.form.toJS(), action.oldId, action.newId)
        .then(json => {
          store.dispatch(setForm(json.form));
          store.dispatch(setErrors(json.errors));
        })
        .catch(error => store.dispatch(setErrors([{severity: 'FATAL', message: error.message}])));
    } else if (action.type === Actions.CREATE_PREVIEW_SESSION) {
      let context = null;
      if (action.context && store.getState().dialobComposer.form.getIn(['metadata', 'composer', 'contextValues'])) {
        context = store.getState().dialobComposer.form.getIn(['metadata', 'composer', 'contextValues']).entrySeq().map(e => ({id: e[0], value: e[1]})).toJS();
      }
      formService.createSession(store.getState().dialobComposer.form.get('_id'), action.language, context)
        .then(json => {
          store.dispatch(redirectPreview(json._id));
        })
        .catch(error => store.dispatch(setErrors([{severity: 'FATAL', message: error.message}])));
    } else if (action.type === Actions.FETCH_VERSIONS) {
      formService.loadVersions(store.getState().dialobComposer.form.get('name'))
        .then(json => {
          store.dispatch(setVersions(json));
        })
        .catch(error => store.dispatch(setErrors([{severity: 'FATAL', message: error.message}])));
    } else if (action.type === Actions.CREATE_NEW_TAG) {
      formService.createTag(store.getState().dialobComposer.form.get('name'), action.name, action.description)
        .then(json => {
          store.dispatch(hideNewTag());
        })
        .catch(error => {
          if (error.message === 'FATAL_409') {
            return store.dispatch(setErrors([{severity: 'FATAL', type: 'TAG_ERROR', message: 'TAG_EXISTS'}], true));
          } else {
            return store.dispatch(setErrors([{severity: 'FATAL', type: 'TAG_ERROR', message: error.message}]));
          }
        });
    } else if (action.type === Actions.COPY_ITEM) {
      formService.duplicateItem(store.getState().dialobComposer.form.toJS(), action.itemId)
          .then(json => {
            store.dispatch(setFormRevision(json.rev));
            store.dispatch(setErrors(json.errors));
            if (json.form) {
              store.dispatch(setForm(json.form));
              store.dispatch(saveForm());
            }
          })
          .catch(error => store.dispatch(setErrors([{severity: 'FATAL', message: error.message}])));
    }
    let result = next(action);
    if (action.saveNeeded === true && !store.getState().dialobComposer.form.get('_tag')) {
      clearTimeout(saveTimer);
      // TODO: show dirty indicator
      saveTimer = setTimeout(() => store.dispatch(saveForm()), SAVE_DELAY);
    }
    return result;
  }
}
