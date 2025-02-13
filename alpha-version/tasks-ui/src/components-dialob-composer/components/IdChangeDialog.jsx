import React, {Component} from 'react';
import {Modal, Button, Input, List, Ref} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {hideChangeId, performChangeId} from '../actions';
import {RESERVED_WORDS, VALID_ID_PATTERN} from '../del/language';

class IdChangeDialog extends Component {

  constructor(props) {
    super(props);
    this.state = {
      valid: true,
      value: null,
      originalId: null,
      processing: false
    };
  }

  static getDerivedStateFromProps(nextProps, prevState) {
    if (prevState.originalId !== nextProps.changeId) {
      return {
        valid: true,
        value: nextProps.changeId,
        originalId: nextProps.changeId,
        processing: false
      };
    } else {
      return prevState;
    }
  }

  validate(value) {
    return (
      value === this.props.changeId || // 'Renaming' to itself is valid (NO-OP)
      (
        VALID_ID_PATTERN.test(value) // Check shape
        && RESERVED_WORDS.indexOf(value) === -1 // Check reserved words
        && (!this.props.items || this.props.items.findKey(v => v.get('id') === value) === undefined) // Check other items
        && (!this.props.variables || this.props.variables.findKey(v => v.get('name') === value) === undefined) // Check variables
      )
    );
  }

  acceptEdit() {
    if (this.props.changeId === this.state.value) {
      this.props.hideChangeId();
    } else {
      this.setState({processing: true});
      this.props.performChangeId(this.props.changeId, this.state.value);
    }
  }

  render() {
    if (this.props.changeId) {
      return (
        <Modal open basic size='small' onClose={() => this.props.hideChangeId()}>
          <Modal.Header>ID Change</Modal.Header>
          <Modal.Content>
            <List bulleted>
              <List.Item>Changing the ID will update all references to this ID.</List.Item>
              <List.Item>Valid ID consists of letters, numbers and underscore.</List.Item>
              <List.Item>Valid ID must start with a letter.</List.Item>
              <List.Item>IDs are case sensitive.</List.Item>
              <List.Item>IDs must be unique.</List.Item>
              <List.Item>IDs must not match reserved words used in expressions.</List.Item>
            </List>
            <Ref innerRef={ref => ref && ref.firstChild.focus()}>
              <Input error={!this.state.valid} fluid value={this.state.value || ''} onChange={(evt) => this.setState({valid: this.validate(evt.target.value), value: evt.target.value})} />
            </Ref>
          </Modal.Content>
          <Modal.Actions>
            <Button loading={this.state.processing} disabled={!this.state.valid} primary onClick={() => this.acceptEdit()}>OK</Button>
            <Button onClick={() => this.props.hideChangeId()}>Cancel</Button>
          </Modal.Actions>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

const IdChangeDialogConnected = connect(
  state => ({
    changeId: state.dialobComposer.editor && state.dialobComposer.editor.get('changeId'),
    items: state.dialobComposer.form && state.dialobComposer.form.get('data'),
    variables: state.dialobComposer.form && state.dialobComposer.form.get('variables')
  }), {
    hideChangeId,
    performChangeId
  }
)(IdChangeDialog);

export {
  IdChangeDialogConnected as default,
  IdChangeDialog
};
