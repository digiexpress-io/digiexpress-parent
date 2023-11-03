import React, {Component} from 'react';
import {Confirm} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {cancelConfirmation} from '../actions';

class ConfirmationDialog extends Component {

  render() {
    if (this.props.confirmableAction) {
      return (
        <Confirm open
          cancelButton={this.props.confirmableAction.confirmCancel || 'Cancel'}
          confirmButton={this.props.confirmableAction.confirmAccept || 'OK'}
          header={this.props.confirmableAction.confirmHeader || null}
          content={this.props.confirmableAction.confirmMesssage || 'Are you sure?'}
          onCancel={() => this.props.cancel()}
          onConfirm={() => this.props.confirm(this.props.confirmableAction)} />
      );
    } else {
      return null;
    }
  }
}

const ConfirmationDialogConnected = connect(
  state => ({
    confirmableAction: state.dialobComposer.editor && state.dialobComposer.editor.get('confirmableAction'),
  }),
  (dispatch, props) => ({
    cancel: () => dispatch(cancelConfirmation()),
    confirm: (action) => dispatch(Object.assign({confirmed: true}, action))
  })
)(ConfirmationDialog);

export {
  ConfirmationDialogConnected as default,
  ConfirmationDialog
};
