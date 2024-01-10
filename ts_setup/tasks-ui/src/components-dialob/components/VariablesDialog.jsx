import React, {Component} from 'react';
import {Modal, Button, Tab, Table, Dropdown, Input, Icon, Checkbox} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {hideVariables, createContextVariable, createExpressionVariable, deleteVariable, showChangeId, updateVariable} from '../actions';
import Immutable from 'immutable';
import CodeEditor from './CodeEditor';

const CONTEXT_TYPES = [
  {key: 'text', text: 'Text', value: 'text'},
  {key: 'number', text: 'Number', value: 'number'},
  {key: 'decimal', text: 'Decimal', value: 'decimal'},
  {key: 'boolean', text: 'Boolean', value: 'boolean'},
  {key: 'date', text: 'Date', value: 'date'},
  {key: 'time', text: 'Time', value: 'time'}
];

const ContextVariables = ({variables, onCreate, onRemove, onIdChange, onChangeAttr, getErrors, readOnly}) => {
  const rows = variables.map((v, k) => <Table.Row key={k} error={getErrors(v.get('name')).size > 0}>
    <Table.Cell collapsing><Button size='tiny' icon='remove' onClick={() => onRemove(v.get('name'))} disabled={readOnly} /></Table.Cell>
    <Table.Cell collapsing textAlign='center' verticalAlign='middle'><Checkbox checked={v.get('published')} onChange={((e, {checked}) => onChangeAttr(v.get('name'), 'published', checked))} /></Table.Cell>
    <Table.Cell selectable><a onClick={() => { if (!readOnly) { onIdChange(v.get('name'))}}}>{v.get('name')}</a></Table.Cell>
    <Table.Cell><Dropdown options={CONTEXT_TYPES} value={v.get('contextType')} onChange={(evt, data) => onChangeAttr(v.get('name'), 'contextType', data.value)} /></Table.Cell>
    <Table.Cell><Input icon={<Icon name='delete' link onClick={(e) => onChangeAttr(v.get('name'), 'defaultValue', null)}/>} transparent fluid value={v.get('defaultValue') || ''} onChange={(e) => onChangeAttr(v.get('name'), 'defaultValue', e.target.value)}/></Table.Cell>
  </Table.Row>);
  return (
    <Tab.Pane>
      <Table celled>
        <Table.Header>
          <Table.Row>
            <Table.HeaderCell collapsing><Button size='tiny' icon='add' onClick={() => onCreate()} disabled={readOnly}/></Table.HeaderCell>
            <Table.HeaderCell collapsing>Published</Table.HeaderCell>
            <Table.HeaderCell>ID</Table.HeaderCell>
            <Table.HeaderCell>Type</Table.HeaderCell>
            <Table.HeaderCell>Default value</Table.HeaderCell>
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {rows}
        </Table.Body>
      </Table>
    </Tab.Pane>
  );
};

const Expressions = ({variables, onCreate, onRemove, onIdChange, onChangeAttr, getErrors, readOnly}) => {
  const rows = variables.map((v, k) => <Table.Row key={k} error={getErrors(v.get('name')).size > 0}>
    <Table.Cell collapsing><Button size='tiny' icon='remove' onClick={() => onRemove(v.get('name'))} disabled={readOnly}/></Table.Cell>
    <Table.Cell collapsing textAlign='center' verticalAlign='middle'><Checkbox checked={v.get('published')} onChange={((e, {checked}) => onChangeAttr(v.get('name'), 'published', checked))} /></Table.Cell>
    <Table.Cell selectable><a onClick={() => { if (!readOnly) { onIdChange(v.get('name'))}}}>{v.get('name')}</a></Table.Cell>
    <Table.Cell>
      <CodeEditor value={v.get('expression') || ''} onChange={(value) => onChangeAttr(v.get('name'), 'expression', value)} readOnly={readOnly} errors={getErrors(v.get('name'))}/>
    </Table.Cell>
  </Table.Row>);
  return (
    <Tab.Pane>
      <Table celled>
        <Table.Header>
          <Table.Row>
            <Table.HeaderCell collapsing><Button size='tiny' icon='add' onClick={() => onCreate()} /></Table.HeaderCell>
            <Table.HeaderCell collapsing>Published</Table.HeaderCell>
            <Table.HeaderCell>ID</Table.HeaderCell>
            <Table.HeaderCell>Expression</Table.HeaderCell>
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {rows}
        </Table.Body>
      </Table>
    </Tab.Pane>
  );
};

class VariablesDialog extends Component {

  getErrors(variableId) {
    return this.props.errors
      ? this.props.errors.filter(e => e.get('itemId') === variableId)
      : new Immutable.List([]);
  }

  render() {
    if (this.props.variablesOpen) {
      const contextVariables = this.props.variables ? this.props.variables.filter(v => v.get('context') === true) : Immutable.List();
      const expressionVariables = this.props.variables ? this.props.variables.filter(v => !v.get('context')) : Immutable.List();
      const tabs = [
        {menuItem: 'Context variables', render: () => <ContextVariables
                                                          variables={contextVariables}
                                                          onCreate={this.props.createContextVariable}
                                                          onRemove={this.props.deleteVariable}
                                                          onIdChange={this.props.showChangeId}
                                                          onChangeAttr={this.props.updateVariable}
                                                          getErrors={this.getErrors.bind(this)}
                                                          readOnly={!this.props.editable}
                                                      />},
        {menuItem: 'Expression variables', render: () => <Expressions
                                                          variables={expressionVariables}
                                                          onCreate={this.props.createExpressionVariable}
                                                          onRemove={this.props.deleteVariable}
                                                          onIdChange={this.props.showChangeId}
                                                          onChangeAttr={this.props.updateVariable}
                                                          getErrors={this.getErrors.bind(this)}
                                                          readOnly={!this.props.editable}
                                                       />}
      ];

      return (
        <Modal open size='large'>
          <Modal.Header>Variables</Modal.Header>
          <Modal.Content scrolling>
            <Tab panes={tabs} />
          </Modal.Content>
          <Modal.Actions>
            <Button primary onClick={() => this.props.hideVariables()}>OK</Button>
          </Modal.Actions>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

const VariablesDialogConnected = connect(
  state => ({
    variablesOpen: state.dialobComposer.editor && state.dialobComposer.editor.get('variablesDialog'),
    variables: state.dialobComposer.form && state.dialobComposer.form.get('variables'),
    errors: state.dialobComposer.editor && state.dialobComposer.editor.get('errors'),
    editable: !state.dialobComposer.form.get('_tag')
  }), {
    hideVariables,
    createContextVariable,
    createExpressionVariable,
    deleteVariable,
    showChangeId,
    updateVariable
  }
)(VariablesDialog);

export {
  VariablesDialogConnected as default,
  VariablesDialog
};
