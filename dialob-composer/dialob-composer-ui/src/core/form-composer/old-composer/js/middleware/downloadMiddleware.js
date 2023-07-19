import * as Actions from '../actions/constants';
import FileSaver from 'file-saver';
import FormService from '../../dialob-client/FormServiceImpl';

export const downloadMiddleware = store => next => action => {
	if (action.type === Actions.DOWNLOAD_FORM) {
    if (!action.tag) {
      const form = store.getState().dialobComposer.form.toJS();
      const json = JSON.stringify(form, null,  2);
      const blob = new Blob([json], {type: 'application/json;charset=utf-8'});
      FileSaver.saveAs(blob, `${form._id}.json`);
    } else {
      let config = store.getState().dialobComposer.config.transport;
      const formService = new FormService(config.apiUrl, config.csrf, config.tenantId);
      const formName = store.getState().dialobComposer.form.get('name');
      formService.loadForm(formName, action.tag).then(json => {
        const text =  JSON.stringify(json, null,  2);
        const blob = new Blob([text], {type: 'application/json;charset=utf-8'});
        FileSaver.saveAs(blob, `${formName}-${action.tag}.json`);
      })
    }
  }
	return next(action);
}
