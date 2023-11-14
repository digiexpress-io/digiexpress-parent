import React, {Component} from 'react';
import {Loader, Dimmer} from 'semantic-ui-react';
import {loadForm, setConfig} from './actions';
import MainMenu from './components/MainMenu';
import TreeView from './components/TreeView';
import Editor from './components/Editor';
import ErrorList from './components/ErrorList';
import * as Defaults from './defaults';
import ConfirmationDialog from './components/ConfirmationDialog';
import ItemOptionsDialog from './components/ItemOptionsDialog';
import FormOptionsDialog from './components/FormOptionsDialog';
import VariablesDialog from './components/VariablesDialog';
import IdChangeDialog from './components/IdChangeDialog';
import PreviewContextDialog from './components/PreviewContextDialog';
import ValueSetDialog from './components/ValueSetDialog';
import TranslationDialog from './components/TranslationDialog';
import FatalErrorDialog from './components/FatalErrorDialog';
import VersioningDialog from './components/VersioningDialog';
import NewTagDialog from './components/NewTagDialog';
import {connect} from 'react-redux';
import './del/codemirrorMode';
import { scrollableArea } from '@resys/react-redux-scroll';
import RuleEditor from './components/RuleEditor';

require('./style.css');

class EditorColumn extends Component {
  render() {
    return (
      <div className='composer-editor-content'>
        <Editor />
      </div>
    );
  }
}

const ScrollableEditor = scrollableArea(EditorColumn);

class DialobComposer extends Component {

  componentDidMount() {
    if (!this.config) {
      this.props.setConfig(this.props.configuration);
    }
    if (!this.props.loaded) {
      this.props.loadForm(this.props.formId);
    }
  }

  componentDidUpdate(prevProps) {
    if (prevProps.formId !== this.props.formId) {
      this.props.loadForm(this.props.formId);
    }
  }

  render() {
    const {loaded, errors} = this.props;

    if (!loaded) {
      return (
        <Dimmer active page inverted>
          <Loader size='massive'>Dialob Composer</Loader>
        </Dimmer>
      );
    }
    const marginTop = '42px';
    const paddingBottom = '55px';
    const menuWidth = Defaults.TREE_WIDTH;

    return (
      <React.Fragment>
        <MainMenu />
        <div className='composer-editor-wrapper'>
          <div className='composer-editor-tree'>
            <TreeView marginTop={marginTop} menuWidth={menuWidth} paddingBottom={paddingBottom}/>
          </div>
          <ScrollableEditor />
          <div className='composer-editor-errors'>
            <ErrorList />
          </div>
          <div className='composer-editor-rules'>
            <RuleEditor />
          </div>
        </div>
        <ConfirmationDialog />
        <ItemOptionsDialog />
        <FormOptionsDialog />
        <VariablesDialog />
        <IdChangeDialog />
        <PreviewContextDialog />
        <ValueSetDialog />
        <TranslationDialog />
        <FatalErrorDialog />
        <VersioningDialog />
        <NewTagDialog />
      </React.Fragment>);
  }
}

const DialobComposerConnected = connect(
  state => ({
    config: state.dialobComposer.config,
    loaded: state.dialobComposer.editor.get('loaded'),
    errors: state.dialobComposer.editor && state.dialobComposer.editor.get('errors')
  }),
  {
    loadForm,
    setConfig
  }
)(DialobComposer);

export {
  DialobComposerConnected as default,
  DialobComposer
};
