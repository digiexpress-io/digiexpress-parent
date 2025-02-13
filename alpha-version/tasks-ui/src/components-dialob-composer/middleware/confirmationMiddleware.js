import {askConfirmation, cancelConfirmation} from '../actions';

export const confirmationMiddleware = store => next => action => {
	if (action.confirm && !action.confirmed) {
		return store.dispatch(askConfirmation(action));
	} else if (action.confirmed) {
		store.dispatch(cancelConfirmation());
  }
	return next(action);
}
