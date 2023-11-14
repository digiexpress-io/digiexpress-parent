import * as Actions from '../actions/constants';
import {showPreviewContext, hidePreviewContext, createPreviewSession, setErrors} from '../actions';

export const previewMiddleware = store => next => action => {
  if (action.type === Actions.REQUEST_FORM_PREVIEW) {
    const variables = store.getState().dialobComposer.form.get('variables');
    if (variables && variables.findIndex(v => v.get('context') === true) > -1) {
      return store.dispatch(showPreviewContext());
    } else {
      let language = store.getState().dialobComposer.editor.get('activeLanguage');
      let formId = store.getState().dialobComposer.form.get('_id');
      return store.dispatch(createPreviewSession(language));
    }
  } else if (action.type === Actions.REDIRECT_PREVIEW) {
    let previewUrl = store.getState().dialobComposer.config.transport.previewUrl;
    let win = window.open(`${previewUrl}/${action.sessionId}`);
    if (win) {
      win.focus();
    } else {
      store.dispatch(setErrors([{severity: 'FATAL', message: 'FATAL_POPUP'}]));
    }
    return store.dispatch(hidePreviewContext());
  }
  return next(action);
}
