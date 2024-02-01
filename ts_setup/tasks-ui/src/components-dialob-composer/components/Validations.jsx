import React, {Component} from 'react';
import {Table, Segment, Label, Button, Form, Header} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {createValidation, deleteValidation, updateValidation} from '../actions';
import * as Defaults from '../defaults';
import CodeEditor from './CodeEditor';
import Immutable from 'immutable';

const ValidationRule = ({onChangeMessage, onChangeRule, onRemove, message, rule, getErrors, readOnly}) => {
  return (
    <Segment>
      { readOnly ? null : <Label as='a' ribbon='right' icon='remove' onClick={onRemove} /> }
      <Form>
        <Form.Input label='Message' icon='exclamation circle' fluid value={message || ''} onChange={onChangeMessage}/>
        <Form.Field label='Validation rule' error={getErrors().size > 0} control={CodeEditor} value={rule || ''} onChange={onChangeRule} icon='check' styleClass='bordered' readOnly={readOnly} errors={getErrors()} />
      </Form>
    </Segment>
   );
};

class Validations extends Component {

  getErrors(index) {
    return this.props.errors
      ? this.props.errors.filter(e => e.get('type') === 'VALIDATION' && e.get('itemId') === this.props.item.get('id') && e.get('index') === index)
      : new Immutable.List([]);;
  }

  newValidation(language) {
    this.props.createValidation(this.props.item.get('id'), language);
  }

  removeValidation(index) {
    this.props.deleteValidation(this.props.item.get('id'), index)
  }

  changeValidation(index, attribute, value, language) {
    this.props.updateValidation(this.props.item.get('id'), index, attribute, value, language)
  }

  render() {
    const {validations} = this.props;
    const items = validations ? validations.map((v, index) => <ValidationRule
        key={index}
        message={v.getIn(['message', this.props.language])}
        rule={v.get('rule')}
        onChangeMessage={(evt) => this.changeValidation(index, 'message', evt.target.value, this.props.language)}
        onChangeRule={(value) => this.changeValidation(index, 'rule', value)}
        onRemove={() => this.removeValidation(index)}
        getErrors={this.getErrors.bind(this, index)}
        readOnly={this.props.readOnly}
       /> ) : [];
    return (
      <Table celled attached='bottom' onClick={(evt) => evt.stopPropagation()}>
        <Table.Body>
          <Table.Row>
            <Table.Cell>
              <Header as='h5'>Validation rules</Header>
              {items}
              <Button disabled={this.props.readOnly} onClick={() => this.newValidation(this.props.language)}>Add validation rule</Button>
            </Table.Cell>
          </Table.Row>
        </Table.Body>
      </Table>
    );
  }
}

const ValidationsConnected = connect(
  state => ({
    language: (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE,
    errors: state.dialobComposer.editor && state.dialobComposer.editor.get('errors')
  }),
  {
    createValidation,
    deleteValidation,
    updateValidation,
  }
)(Validations);

export {
  Validations,
  ValidationsConnected as default
}
