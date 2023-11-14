import React, {Component} from 'react';
import {Modal, Button, Message, Icon} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {downloadForm} from '../actions';
import * as Status from '../helpers/constants';

function translateFatal(message) {
  switch (message) {
    case 'FATAL_409':
      return 'Dialog was modified by another user';
    case 'FATAL_403':
      return 'Session was expired, please log in again';
    case 'FATAL_404':
      return 'Dialog does not exist';
    case 'FATAL_POPUP':
      return 'Preview window blocked. Please allow pop-ups for preview';
    default:
      return 'Communication problem';
  }
}

class FatalErrorDialog extends Component {

  render() {
    if (this.props.status === Status.STATUS_FATAL) {
      const error = this.props.errors.find(e => e.get('severity') === 'FATAL');
      return (
        <Modal open basic>
          <Message error icon floating size='big'>
            <Icon name='bomb' />
            <Message.Content>
              <Message.Header>Fatal error</Message.Header>
              {translateFatal(error && error.get('message'))}
            </Message.Content>
          </Message>
          <Button icon labelPosition='left' disabled={!this.props.formLoaded} onClick={() => this.props.downloadForm()}><Icon name='download' />Download</Button>
          <Button icon labelPosition='left' onClick={() => location.reload(true)}><Icon name='refresh' />Reload</Button>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

const FatalErrorDialogConnected = connect(
  state => ({
    status: state.dialobComposer.editor && state.dialobComposer.editor.get('status'),
    formLoaded: state.dialobComposer.form && state.dialobComposer.form.get('_id'),
    errors: state.dialobComposer.editor && state.dialobComposer.editor.get('errors')
  }), {
    downloadForm
  }
)(FatalErrorDialog);

export {
  FatalErrorDialogConnected as default,
  FatalErrorDialog
};
