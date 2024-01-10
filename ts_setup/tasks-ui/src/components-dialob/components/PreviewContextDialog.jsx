import React, {Component} from 'react';
import {Modal, Button, Table, Input, Icon} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {hidePreviewContext, setContextValue, createPreviewSession} from '../actions';
import Immutable from 'immutable';
import * as Defaults from '../defaults';

class PreviewContextDialog extends Component {

  render() {
    if (this.props.previewContextOpen) {
      const contextVariables = this.props.variables ? this.props.variables.filter(v => v.get('context') === true) : Immutable.List();
      const rows = contextVariables.map((v, key) => <Table.Row key={key}>
          <Table.Cell>{v.get('name')}</Table.Cell>
          <Table.Cell>
            <Input transparent fluid
              placeholder={v.get('defaultValue')}
              value={(this.props.contextValues && this.props.contextValues.get(v.get('name'))) || ''}
              onChange={(evt) => this.props.setContextValue(v.get('name'), evt.target.value)}
              icon={<Icon name='delete' link onClick={(e) => this.props.setContextValue(v.get('name'), null)}/>} iconPosition='right'/>
          </Table.Cell>
        </Table.Row>);
      return (
        <Modal open>
          <Modal.Header>Preview</Modal.Header>
          <Modal.Content scrolling>
            <label>Values for context variables</label>
            <Table celled>
              <Table.Header>
                <Table.Row>
                  <Table.HeaderCell collapsing>ID</Table.HeaderCell>
                  <Table.HeaderCell>Value</Table.HeaderCell>
                </Table.Row>
              </Table.Header>
              <Table.Body>
                {rows}
              </Table.Body>
            </Table>
          </Modal.Content>
          <Modal.Actions>
            <Button primary onClick={() => this.props.createPreviewSession(this.props.language, true)}>Preview</Button>
            <Button onClick={() => this.props.hidePreviewContext()}>Cancel</Button>
          </Modal.Actions>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

const PreviewContextDialogConnected = connect(
  state => ({
    previewContextOpen: state.dialobComposer.editor && state.dialobComposer.editor.get('previewContextDialog'),
    variables: state.dialobComposer.form && state.dialobComposer.form.get('variables'),
    contextValues: state.dialobComposer.form && state.dialobComposer.form.getIn(['metadata', 'composer', 'contextValues']),
    language: (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE
  }), {
    hidePreviewContext,
    setContextValue,
    createPreviewSession
  }
)(PreviewContextDialog);

export {
  PreviewContextDialogConnected as default,
  PreviewContextDialog
};
