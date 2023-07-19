import React, {Component} from 'react';
import {connect} from 'react-redux';
import {updateItem} from '../../actions'
import * as Defaults from '../../defaults';
import {MarkdownEditor} from '../MarkdownEditor';

class Description extends Component {

  render() {
    const value = this.props.item.getIn(['description', this.props.language]) || '';
    return (
      <MarkdownEditor onChange={(v) => this.props.setAttribute('description', v, this.props.language)} value={value} />
    );
  }
}

const DescriptionConnected = connect(
  state => ({
    language: (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE,
  }),
  (dispatch, props) => ({
    setAttribute: (attribute, value, language = null) => dispatch(updateItem(props.item.get('id'), attribute, value, language)),
  })
)(Description);

export {
  DescriptionConnected as default,
  Description
};
