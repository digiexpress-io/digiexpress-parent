import React, {Component} from 'react';
import CodeMirrorIntegration from 'codemirror';
import debounce from 'lodash.debounce';
import delHinter from '../del/hinting';
import {connect} from 'react-redux';
import classnames from 'classnames';
import { translateErrorMessage } from '../helpers/utils';

import 'codemirror/lib/codemirror.css';
import 'codemirror/addon/lint/lint.css';
import 'codemirror/addon/hint/show-hint.css';
import 'codemirror/addon/lint/lint';
import 'codemirror/addon/hint/show-hint';
import 'codemirror/addon/display/placeholder';

class CodeEditor extends Component {

  constructor(props) {
    super(props);
    this.editor = null;
    this.state = {
      focused: false
    };
  }

  handleChange = debounce(value => {
    if (this.props.onChange) {
      this.props.onChange(value);
    }
  }, 250);

  getHints(editor, options) {
    const cursor = editor.getCursor();
    const line = editor.getLine(cursor.line);
    const token = editor.getTokenAt(cursor);
    const pos = {
      id: cursor.line,
      content: token.string,
      at: cursor.ch,
      isEmpty: token.string.length === 0 || !token.string.trim()
    };

    return {
      list: options.getList(pos),
      from: CodeMirrorIntegration.Pos(cursor.line, pos.isEmpty ? token.end : token.start ),
      to: CodeMirrorIntegration.Pos(cursor.line, token.end)
    };
  }

  getAnnotations(content, updateLinting, options, cm) {
    let annotations = [];
    if (this.props.errors) {
      annotations = this.props.errors.map(e => (
        {
          message: translateErrorMessage(e),
          severity: e.get('level') === 'ERROR' ? 'error' : 'warning',
          from: cm.getDoc().posFromIndex(e.get('startIndex')),
          to: cm.getDoc().posFromIndex(e.get('endIndex') + 1)
        }
      )).toJS();
    }
    updateLinting(annotations, content);
  }

  componentDidMount() {
    const element = this.element;
    if (element) {

        const hintOptions = {
          completeSingle: false,
          hint: this.getHints,
          getList: this.props.hinter
        };

        const lintOptions = {
          getAnnotations: this.getAnnotations.bind(this),
          async: true
        };

        const editor = CodeMirrorIntegration.fromTextArea(element, {
          mode: 'del',
          autofocus: true,
          lineNumbers: true,
          lineWrapping: true,
          readOnly: this.props.readOnly,
          tabSize: 2,
          extraKeys: {
            'Ctrl-Space': 'autocomplete'
          },
          hintOptions,
          lint: lintOptions
        });

        editor.on('change', e => this.handleChange(e.getValue()));
        editor.on('focus', () =>  this.setState({focused: true}));
        editor.on('blur', () => this.setState({focused: false}));

        this.editor = editor;
    }
  }

  focusEditor() {
    if (this.editor) {
      this.editor.focus();
    }
  }

  componentDidUpdate(prevProps, prevState) {
    if (this.editor) {
      this.editor.setOption('readOnly', this.props.readOnly);
      this.editor.performLint(); // TODO: Only if errors changed
      if (prevProps.value !== this.props.value) {
        const cPos = this.editor.hasFocus() ? this.editor.getCursor() : null;
        this.editor.setValue(this.props.value);
        if (this.editor.hasFocus()) {
          this.editor.setCursor(cPos);
        }
      }
    }
  }

  render() {
    const {id, value, icon, styleClass} = this.props;
    return (
      <div className={classnames('ui fluid', {'icon': !!icon}, 'input', styleClass, {'focused': this.state.focused})} onClick={() => this.focusEditor()} >
        <textarea className='testclass' rows={2} id={id} ref={element => this.element = element} defaultValue={value} />
        {icon &&
          <i aria-hidden='true' className={classnames(icon, 'icon')} />
        }
      </div>
    );
  }
}

const CodeEditorConnected = connect(
  state => ({
    hinter: context => delHinter(context, state.dialobComposer.form)
  }),
  {
  }
)(CodeEditor);

export {
  CodeEditorConnected as default,
  CodeEditor
};
