import React, {Component} from 'react';
import {Tab, Header } from 'semantic-ui-react';
import {connect} from 'react-redux';
import {updateItem} from '../actions';
import CodeEditor from './CodeEditor';
import Immutable from 'immutable';

const NOT_EDITABLE_TYPES = ['group', 'rowgroup', 'surveygroup', 'note'];

class RuleEditor extends Component {

  setAttribute(attribute, value, language = null) {
    this.props.updateItem(this.props.activeItemId, attribute, value, language);
  }

  getErrors() {
    const itemId = this.props.activeItemId;
    const item = this.props.getItem();
    return this.props.errors
    ? this.props.errors.filter(e => (e.get('message').startsWith('VALUESET_') && e.get('itemId') === item.get('valueSetId')) || e.get('itemId') === itemId)
    : new Immutable.List([]);
  }

  render() {
    const {activeItemId, getItem} = this.props;

    if (activeItemId) {
      const item = getItem();
      if (!item) {
        return null;
      }

      const errors = this.getErrors();

      const visibilityErrors = errors.filter(e => e.get('type') === 'VISIBILITY');

      const panes = [
        {
          menuItem: {key: 'activeWhen', icon: 'eye', content: 'Visibility', color: visibilityErrors.size > 0 ? 'red' : 'black'},
          render: () => <Tab.Pane><CodeEditor active={true} styleClass='dialob-window-codeedit' value={item.get('activeWhen') || ''} onChange={value => this.setAttribute('activeWhen', value)} placeholder='Visibility' errors={visibilityErrors}/></Tab.Pane>
        }
      ];

      if (NOT_EDITABLE_TYPES.indexOf(item.get('type')) < 0) {
        const requirementErrors = errors.filter(e => e.get('type') === 'REQUIREMENT');
        panes.push( {
          menuItem: {key: 'required', icon: 'gavel', content: 'Requirement', color: requirementErrors.size > 0 ? 'red' : 'black'},
          render: () => <Tab.Pane><CodeEditor active={true} styleClass='dialob-window-codeedit' value={item.get('required') || ''} onChange={value => this.setAttribute('required', value)} placeholder='Required' errors={requirementErrors}/></Tab.Pane>
        });
      }

      return (
        <React.Fragment>
          <Header as='h5' style={{marginBottom: '5px'}}>Rules for <em>{activeItemId}</em></Header>
          <Tab menu={{fluid: true, vertical: true, tabluar: 'left'}} panes={panes} />
        </React.Fragment>
      );
    } else {
      return null;
    }
  }
}

const RuleEditorConnected = connect(
  state => ({
    activeItemId: state.dialobComposer.editor && state.dialobComposer.editor.get('activeItemId'),
    errors: state.dialobComposer.editor && state.dialobComposer.editor.get('errors'),
    get getItem() { return () => state.dialobComposer.form && this.activeItemId && state.dialobComposer.form.getIn(['data', this.activeItemId]); }
  }), {
    updateItem
  }
)(RuleEditor);

export {
  RuleEditorConnected as default,
  RuleEditor
};
