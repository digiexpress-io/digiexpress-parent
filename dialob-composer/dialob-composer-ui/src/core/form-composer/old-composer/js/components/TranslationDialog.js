import React, {Component} from 'react';
import {Modal, Button, Label, Table, Tab, Segment, Dropdown, TextArea} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {hideTranslation, updateItem, updateValuesetEntry, updateValidation, addLanguage, setActiveLanguage, deleteLanguage} from '../actions';
import { Understood } from '../../understood';
import * as Defaults from '../defaults';
import {MarkdownEditor} from './MarkdownEditor';
import md_strip_tags from 'remove-markdown';

class LanguageConfigurator extends Component {

  state = {
    selectedLanguage: null
  };

  render() {
    const rows = this.props.languages.map((l, i) => {
        const name = Defaults.LANGUAGES.find(c => c.code === l).name;
        return (
          <Table.Row key={i}>
            <Table.Cell onClick={() => this.props.setActiveLanguage(l)}>{l == this.props.activeLanguage ? <Label ribbon color='blue'>{name}</Label>: name}</Table.Cell>
            <Table.Cell><Button size='mini' disabled={l == this.props.activeLanguage} onClick={() => this.props.deleteLanguage(l)}>Delete</Button></Table.Cell>
          </Table.Row>);
      }
    );
    return (
      <Table celled>
        <Table.Header>
          <Table.Row>
            <Table.HeaderCell>Language</Table.HeaderCell>
            <Table.HeaderCell collapsing />
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {rows}
        </Table.Body>
        <Table.Footer>
          <Table.Row>
            <Table.HeaderCell>Add new language: <Dropdown search selection
                options={this.props.newLanguages}
                value={this.state.selectedLanguage}
                onChange={(evt, data) => this.setState({selectedLanguage: data.value})} /></Table.HeaderCell>
            <Table.HeaderCell singleLine>
              <Button size='mini' onClick={() => this.props.addLanguage(this.state.selectedLanguage, this.props.activeLanguage)} disabled={!this.state.selectedLanguage}>Copy from active</Button>
              <Button size='mini' onClick={() => this.props.addLanguage(this.state.selectedLanguage)} disabled={!this.state.selectedLanguage}>Create empty</Button>
            </Table.HeaderCell>
          </Table.Row>
        </Table.Footer>
      </Table>
    );
  }
}

const TranslationEditor = ({value, onChange, metadata}) => {
  if (metadata.richText) {
    return (
      <MarkdownEditor onChange={(v) => onChange(v)} value={value} /> 
    );
  } else {
    return (
        <TextArea value={value} onChange={e => onChange(e.currentTarget.value)}/>
    );
  }
}

const Translator = ({translations, metadata, initialLanguage, onChange, languages}) => {
  return (
    <Segment basic style={{ height: 500 }}>
      <Understood
        initialTranslations={translations}
        format='keyToLanguage'
        initialLanguages={[initialLanguage]}
        onChangeItem={onChange}
        availableLanguages={languages}
        initialMetadata={metadata}
        components={{ Editor: TranslationEditor }}
        plaintextTranslation={md_strip_tags}
      />
    </Segment>
  );
};

class TranslationDialog extends Component {

  getItemTranslations() {
    let translations = {};
    let metadata = {key : {}};
    this.props.items.forEach((v, k) => {
      if (v.get('label')) {
        const key = `i:${v.get('id')}:l`;
        translations[key] = v.get('label').toJS();
        metadata.key[key] = {description: 'Item label', richText: v.get('type') === 'note'};
      }
      if (v.get('description')) {
        const key = `i:${v.get('id')}:d`;
        translations[key] = v.get('description').toJS();
        metadata.key[key] = {description: 'Item description', richText: true};
      }
      if (v.get('validations')) {
        v.get('validations').forEach((val, idx) => {
          const key = `i:${v.get('id')}:v:${idx}`;
          translations[key] = val.get('message').toJS();
          metadata.key[key] = {description: 'Validation'};
          return true;
        });
      }
      return true;
    });
    return {translations, metadata};
  }

  getValueSetTranslations() {
    if (this.props.valueSets) {
      let translations = {};
      let metadata = {key : {}};
      this.props.valueSets.forEach((vs, k) => {
        if (vs.get('entries')) {
          vs.get('entries').forEach((e, k) => {
            const key = `v:${vs.get('id')}:${k}:${e.get('id')}`;
            translations[key] = e.get('label').toJS();
            metadata.key[key] = {description: 'Valueset entry'};
            return true;
          });
        }
        return true;
      });
      return {translations, metadata};
    } else {
      return {translations: {}, metadata: {}};
    }
  }

  updateTranslation(key, language, text) {
    const keyTokens = key.split(':');
    if (keyTokens[0] === 'i') {
      // Item
      let attribute = '';
      if (keyTokens[2] === 'l') {
        this.props.updateItem(keyTokens[1], 'label', text, language);
      } else if (keyTokens[2] === 'd') {
        this.props.updateItem(keyTokens[1], 'description', text, language);
      } else if (keyTokens[2] === 'v') {
        this.props.updateValidation(keyTokens[1], parseInt(keyTokens[3]), 'message', text, language);
      }
    } else if (keyTokens[0] === 'v') {
        // Valueset
        this.props.updateValuesetEntry(keyTokens[1], parseInt(keyTokens[2]), null, text, language);
    }
  }

  render() {
    if (this.props.translationOpen) {
      const newLanguages = Defaults.LANGUAGES
          .filter(lang => !this.props.formLanguages.contains(lang.code))
          .map(lang => ({key: lang.code, text: lang.name, value: lang.code}));

      const languages = {};
      this.props.formLanguages.forEach(l => {
        const lang = Defaults.LANGUAGES.find(c => c.code === l);
        languages[l] = {
          longName: lang.name || l,
          flag: lang.flag || l
        };
      })
      let itemTranslations = this.getItemTranslations();
      let valueSetTranslations = this.getValueSetTranslations();
      itemTranslations.metadata.language = languages;
      valueSetTranslations.metadata.language = languages;

      const panes = [
        {menuItem: 'Languages', render: () => <LanguageConfigurator
                                               languages={this.props.formLanguages}
                                               activeLanguage={this.props.language}
                                               newLanguages={newLanguages}
                                               addLanguage={this.props.addLanguage}
                                               setActiveLanguage={this.props.setActiveLanguage}
                                               deleteLanguage={this.props.deleteLanguage}
                                              />
        },
        {menuItem: 'Fields', render: () => <Translator
                                              key='fields'
                                              translations={itemTranslations.translations}
                                              metadata={itemTranslations.metadata}
                                              initialLanguage={this.props.language}
                                              onChange={this.updateTranslation.bind(this)}
                                              languages={this.props.formLanguages.toJS()} />
        },
        {menuItem: 'Lists', render: () =>  <Translator
                                              key='lists'
                                              translations={valueSetTranslations.translations}
                                              metadata={valueSetTranslations.metadata}
                                              initialLanguage={this.props.language}
                                              onChange={this.updateTranslation.bind(this)}
                                              languages={this.props.formLanguages.toJS()} />
        }
      ];

      return (
        <Modal open size='large' centered={false}>
          <Modal.Header>Translation</Modal.Header>
          <Modal.Content >
            <Tab panes={panes} />
          </Modal.Content>
          <Modal.Actions>
            <Button primary onClick={() => this.props.hideTranslation()}>OK</Button>
          </Modal.Actions>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

const TranslationDialogConnected = connect(
  state => ({
    translationOpen: state.dialobComposer.editor && state.dialobComposer.editor.get('translationOpen'),
    language: (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE,
    formLanguages: state.dialobComposer.form.getIn(['metadata', 'languages']),
    items: state.dialobComposer.form && state.dialobComposer.form.get('data'),
    valueSets: state.dialobComposer.form && state.dialobComposer.form.get('valueSets')
  }), {
    hideTranslation,
    updateItem,
    updateValuesetEntry,
    updateValidation,
    addLanguage,
    setActiveLanguage,
    deleteLanguage
  }
)(TranslationDialog);

export {
  TranslationDialogConnected as default,
  TranslationDialog
};
