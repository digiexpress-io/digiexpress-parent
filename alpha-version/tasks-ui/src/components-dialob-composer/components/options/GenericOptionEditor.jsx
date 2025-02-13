import React, {Component} from 'react';
import {Segment, Form} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {updateItem} from '../../actions';
import * as Defaults from '../../defaults';

class GenericOptionEditor extends Component {
  render() {
    const {item, updateItem, language} = this.props;
    return (
      <Segment>
        <Form>
          <Form.Field>
            <Form.Input fluid label='Additional attribute' value={item.getIn(['attr', language]) || ''} onChange={(evt) =>
                 updateItem(item.get('id'), 'attr', evt.target.value, language)} />
          </Form.Field>
        </Form>
      </Segment>
    );
  }
}

const GenericOptionEditorConnected = connect(
  state => ({
    language: (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE,
  }), {
    updateItem
  }
)(GenericOptionEditor);

export {
  GenericOptionEditorConnected as default,
  GenericOptionEditor
};
