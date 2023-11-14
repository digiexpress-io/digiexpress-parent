import React, { Component } from "react";
import { Modal, Button, Form, Message } from "semantic-ui-react";
import { connect } from "react-redux";
import { hideNewTag, createNewTag } from "../actions";
import { translateErrorMessage } from '../helpers/utils';

class NewTagDialog extends Component {

  constructor(props) {
    super(props);
    this.state = {
      tag: '',
      description: ''
    };
  }

  componentWillUpdate(nextProps) {
    if (!this.props.newTagOpen && nextProps.newTagOpen) {
      this.setState({tag: ''});
    }
  }

  render() {
    if (this.props.newTagOpen) {

      const tagErrors = this.props.errors &&
          this.props.errors
            .filter(e => e.get('message').startsWith('TAG_'));

      const errorList = tagErrors && tagErrors.size > 0 &&
      <Message error header='Errors'
          list={tagErrors.map(e => translateErrorMessage(e)).toJS()} />;

      return (
        <Modal open size='tiny'>
          <Modal.Header>New Tag</Modal.Header>
          <Modal.Content>
            <Form>
              <Form.Field>
                <Form.Input label='New tag name' fluid focus value={this.state.tag} onChange={(e, d) => this.setState({tag: d.value}) }/>
              </Form.Field>
              <Form.Field>
                <Form.TextArea label='Description' fluid value={this.state.descrption} onChange={(e, d) => this.setState({description: d.value}) }/>
              </Form.Field>
            </Form>
            {errorList}
          </Modal.Content>
          <Modal.Actions>
            <Button primary onClick={() => this.props.createNewTag(this.state.tag, this.state.description)}>
              OK
            </Button>
            <Button onClick={() => this.props.hideNewTag()}>
              Cancel
            </Button>
          </Modal.Actions>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

const NewTagDialogConnected = connect(
  state => ({
    newTagOpen:
      state.dialobComposer.editor &&
      state.dialobComposer.editor.get('newTagDialog'),
    errors: state.dialobComposer.editor && state.dialobComposer.editor.get('errors')
  }),
  {
    hideNewTag,
    createNewTag
  }
)(NewTagDialog);

export { NewTagDialogConnected as default, NewTagDialog };
