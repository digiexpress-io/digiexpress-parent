import React, {Component} from 'react';
import {Container, Menu, Icon, Dropdown, Popup} from 'semantic-ui-react';
import {connect} from 'react-redux';
import * as Defaults from '../defaults';
import {setActiveLanguage, showFormOptions, showVariables, requestPreview, downloadForm, showValueSets, showTranslation, closeEditor, showVersioning, showNewTag} from '../actions';
import StatusIndicator from './StatusIndicator';
import * as Status from '../helpers/constants';
import SearchMenu from './SearchMenu';

class MainMenu extends Component {

  getLanguages() {
    return Defaults.LANGUAGES
      .filter(lang => this.props.formLanguages && this.props.formLanguages.contains(lang.code))
      .map((lang, i) =>
        <Dropdown.Item key={i} active={lang.code === this.props.language} onClick={() => this.props.setActiveLanguage(lang.code)}>{lang.name}</Dropdown.Item>);
  }

  getLanguageName(code) {
    return Defaults.LANGUAGES.find(lang => lang.code === code).name;
  }

  render() {
    const formTag = this.props.formTag || 'LATEST';
    return (
      <Container>
        <Menu fixed='top'>
          <Menu.Item header>
              Dialob Composer
              &nbsp;<small>{this.props.formLabel}</small>
          </Menu.Item>

          <Menu.Item onClick={this.props.showTranslation}>
            Translations
          </Menu.Item>
          <Menu.Item onClick={this.props.showVariables}>
            Variables
          </Menu.Item>
          <Menu.Item onClick={this.props.showValueSets}>
            Lists
          </Menu.Item>
          <Menu.Item onClick={this.props.showFormOptions}>
            Options
          </Menu.Item>
          <Dropdown item text={`Version: ${formTag}`} lazyLoad>
            <Dropdown.Menu>
              <Dropdown.Item onClick={this.props.showVersioning}>Manage versions...</Dropdown.Item>
              <Dropdown.Item disabled={formTag !== 'LATEST' || (this.props.status !== Status.STATUS_OK && this.props.status !== Status.STATUS_WARNINGS)} onClick={() => this.props.showNewTag()}>Create version tag</Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>
          <Menu.Item onClick={() => {
              let win = window.open(this.props.config.documentationUrl || 'https://docs.dialob.io/');
              if (win) {
                win.focus();
              }
             }}>
            <Popup
              trigger={<Icon name='life ring outline' />}
              content='Dialob Composer User Guide'
              on='hover' />
          </Menu.Item>
          <Menu.Menu position='right'>
              <SearchMenu />
              <Menu.Item onClick={()=>this.props.downloadForm(null)}>
                <Popup
                  trigger={<Icon name='download' />}
                  content='Download dialog as JSON'
                  on='hover' />
              </Menu.Item>
              <Menu.Item>
                <StatusIndicator status={this.props.status}/>
              </Menu.Item>
              <Dropdown item text={this.getLanguageName(this.props.language)} lazyLoad>
                <Dropdown.Menu>
                  {this.getLanguages()}
                </Dropdown.Menu>
              </Dropdown>
              {
                this.props.config && this.props.config.transport.previewUrl &&
                <Menu.Item disabled={this.props.status !== Status.STATUS_OK && this.props.status !== Status.STATUS_WARNINGS} onClick={this.props.requestPreview}><Icon name='eye' /> Preview</Menu.Item>
              }
              {
                this.props.config && this.props.config.closeHandler &&
               <Menu.Item icon='close' onClick={this.props.closeEditor} />
              }
          </Menu.Menu>
        </Menu>
      </Container>
    );
  }
}

const MainMenuConnected = connect(
  state => ({
    status: state.dialobComposer.editor && state.dialobComposer.editor.get('status'),
    language: (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE,
    formLanguages: state.dialobComposer.form.getIn(['metadata', 'languages']),
    formLabel: state.dialobComposer.form.getIn(['metadata', 'label']),
    formTag: state.dialobComposer.form.get('_tag'),
    config: state.dialobComposer.config
  }),
  {
    setActiveLanguage,
    showFormOptions,
    showVariables,
    requestPreview,
    downloadForm,
    showValueSets,
    showTranslation,
    closeEditor,
    showVersioning,
    showNewTag
  }
)(MainMenu);

export {
  MainMenuConnected as default,
  MainMenu
};
