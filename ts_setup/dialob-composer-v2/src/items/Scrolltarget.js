import React from 'react';
import {scrollToWhen} from '@resys/react-redux-scroll';
import * as Actions from '../actions/constants';

const isItemActivated = (action, props) =>
  action.type === Actions.SET_ACTIVE_ITEM && action.itemId === props.itemId && !action.noScroll;

const Scrolltarget = scrollToWhen({
  pattern: isItemActivated,
  scrollOptions: {
    yMargin: 50
  },
  excludedProps: ['itemId']
})('div');

export {
  Scrolltarget as default
};
