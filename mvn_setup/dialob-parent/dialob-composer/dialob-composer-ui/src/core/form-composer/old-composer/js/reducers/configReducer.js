import * as Actions from '../actions/constants';

export function configReducer(state = {}, action) {
  if (action.type === Actions.SET_CONFIG) {
    return state = action.config;
  }
  return state;
}
