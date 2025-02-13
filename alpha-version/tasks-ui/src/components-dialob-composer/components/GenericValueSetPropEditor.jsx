import React, { Component } from 'react';
import {Input} from 'semantic-ui-react';
import * as Defaults from '../defaults';
import {connect} from 'react-redux';

class GenericValueSetPropEditor extends Component {

  render() {
    const {value, onChange, language} = this.props;
    const currentValue = value ? value.toJS() : {};
    const displayValue = currentValue[language] || '';

    const createValue = v => {
      currentValue[language] = v;
      return currentValue;
    }

    return (
      <Input transparent fluid value={displayValue} onChange={e => onChange(createValue(e.target.value))} />
    );
  }

}

const GenericValueSetPropEditorConnected = connect(
  state => ({
    language: (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE,
  }), {
  }
)(GenericValueSetPropEditor);

export {
  GenericValueSetPropEditorConnected as default,
  GenericValueSetPropEditor
};
