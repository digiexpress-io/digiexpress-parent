import React from 'react';
import {itemFactory} from '.';
import {connect} from 'react-redux';
import {setActiveItem, addItem, updateItem, deleteItem, showChangeId, setActivePage, setTreeCollapse} from '../actions';
import * as Defaults from '../defaults';
import * as Status from '../helpers/constants';
import Immutable from 'immutable';

class Item extends React.Component {

  constructor(props) {
    super(props);
    this.deleteItem = this.props.deleteItem.bind(this, props.itemId);
  }

  createChildren(props, config) {
    return this.props.item.get('items') && this.props.item.get('items')
      .map(itemId => this.props.getItemById(itemId))
      .map(item => itemFactory(item, props, config || this.props.itemEditors));
  }

  getErrors() {
    return this.props.errors
      ? this.props.errors.filter(e => (e.get('message').startsWith('VALUESET_') && e.get('itemId') === this.props.item.get('valueSetId')) || e.get('itemId') === this.props.itemId)
      : new Immutable.List([]);
  }

  getErrorLevel() {
    const errors = this.getErrors();
    if (errors.size === 0) {
      return Status.STATUS_OK;
    } else  if (errors.size === errors.filter(e => e.get('level') === 'WARNING').size) {
      return Status.STATUS_WARNINGS;
    } else {
      return Status.STATUS_ERRORS;
    }
  }

  getBorderColor() {
    const errorLevel = this.getErrorLevel();
    if (this.props.active) {
      return 'blue';
    } else if (errorLevel === Status.STATUS_WARNINGS) {
      return 'yellow';
    } else if (errorLevel === Status.STATUS_ERRORS) {
      return 'red';
    } else {
      return null;
    }

  }

  setActive(noScroll = false) {
    if (this.props.isPage) {
      this.props.setActivePage(this.props.itemId);
    } else {
      this.props.setActiveItem(this.props.itemId, noScroll);
    }
  }

  newItem(config, parentItemId, afterItemId) {
    this.props.addItem(config, parentItemId, afterItemId);
  }

  setAttribute(attribute, value, language = null) {
    this.props.updateItem(this.props.itemId, attribute, value, language);
  }

  changeId() {
    this.props.showChangeId(this.props.itemId);
  }

  setActivePage(pageId) {
    this.props.setActivePage(pageId);
  }

  setTreeCollapsed(collapsed) {
    this.props.setTreeCollapse(this.props.itemId, collapsed);
  }

}

function connectItem(component) {
  return connect(
    (state, props) => {
      const item = state.dialobComposer.form.getIn(['data', props.itemId]);
      return ({
        item,
        active: item && state.dialobComposer.editor && props.itemId === state.dialobComposer.editor.get('activeItemId'),
        language: (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE,
        errors: state.dialobComposer.editor && state.dialobComposer.editor.get('errors'),
        itemEditors: state.dialobComposer.config.itemEditors,
        editable: !state.dialobComposer.form.get('_tag'),
        treeCollapsed: state.dialobComposer.editor && state.dialobComposer.editor.get('treeCollapse') && state.dialobComposer.editor.get('treeCollapse').findIndex(id => id === props.itemId) > -1,
        rootItemId: state.dialobComposer.editor.get('rootItemId'),
        validations: item && item.get('validations'),
        globalValueSets: state.dialobComposer.form && state.dialobComposer.form.getIn(['metadata', 'composer', 'globalValueSets']),
      });
    }
    ,
    {
      setActiveItem,
      addItem,
      updateItem,
      deleteItem,
      showChangeId,
      setActivePage,
      setTreeCollapse
    }
  )(component);
}

export {
  Item as default,
  connectItem
};