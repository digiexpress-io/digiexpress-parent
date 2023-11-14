import React, {useRef, useState, useEffect} from 'react';
import {Modal, Button, Label, Table, Tab, Segment, Dropdown, TextArea} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {hideTranslation, updateItem, updateValuesetEntry, updateValidation, addLanguage, setActiveLanguage, deleteLanguage} from '../actions';
import { Understood } from 'components-dialob/understood';
import * as Defaults from '../defaults';
import {MarkdownEditor} from './MarkdownEditor';
import md_strip_tags from 'remove-markdown';
import Papa from 'papaparse';
import FileSaver from 'file-saver';

const LanguageConfigurator = ({
  languages,
  activeLanguage,
  newLanguages,
  addLanguage,
  setActiveLanguage,
  deleteLanguage
}) => {
  const  [selectedLanguage, setSelectedLanguage] = useState(null);

  const rows = languages.map((l, i) => {
    const name = Defaults.LANGUAGES.find(c => c.code === l).name;
      return (
        <Table.Row key={i}>
          <Table.Cell onClick={() => setActiveLanguage(l)}>{l == activeLanguage ? <Label ribbon color='blue'>{name}</Label>: name}</Table.Cell>
          <Table.Cell><Button size='mini' disabled={l == activeLanguage} onClick={() => deleteLanguage(l)}>Delete</Button></Table.Cell>
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
              options={newLanguages}
              value={selectedLanguage}
              onChange={(evt, data) => setSelectedLanguage(data.value)} /></Table.HeaderCell>
          <Table.HeaderCell singleLine>
            <Button size='mini' onClick={() => addLanguage(selectedLanguage, activeLanguage)} disabled={!selectedLanguage}>Copy from active</Button>
            <Button size='mini' onClick={() => addLanguage(selectedLanguage)} disabled={!selectedLanguage}>Create empty</Button>
          </Table.HeaderCell>
        </Table.Row>
      </Table.Footer>
    </Table>
  );
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

const TranslationDialog = (props) => {
  const fileInputRef = useRef();

  const parse = (inputFile) => {
    return new Promise((resolve, reject) => {
      Papa.parse(inputFile, {
        header: false,
        transformHeader: h => h.trim(),
        skipEmptyLines: true,
        error: (error) => {
          console.error('CSV Parse error', error);
          reject(error);
        },
        complete: (results) => {
          resolve(results);
        }
      });
    });
  };

  const validateParsedFile = (data) => {
    if(data[0][0] !== props.formLabel || data[1][0] !== 'Item ID' || data[1][1] !== 'Description') 
      return false
    for(let i=2; i<data[1].length; i++){
      if(data[1][i].length !== 2) 
        return false
    }
    return true
  }

  const updateTranslation = (key, language, text) => {
    const keyTokens = key.split(':');
    if (keyTokens[0] === 'i') {
      // Item
      let attribute = '';
      if (keyTokens[2] === 'l') {
        props.updateItem(keyTokens[1], 'label', text, language);
      } else if (keyTokens[2] === 'd') {
        props.updateItem(keyTokens[1], 'description', text, language);
      } else if (keyTokens[2] === 'v') {
        props.updateValidation(keyTokens[1], parseInt(keyTokens[3]), 'message', text, language);
      }
    } else if (keyTokens[0] === 'v') {
        // Valueset
        props.updateValuesetEntry(keyTokens[1], parseInt(keyTokens[2]), null, text, language);
    }
  }

  const fileChange = async (event) => {
    const csvResult = await parse(event.target.files[0]);
    const { data } = csvResult;
    const languages = props.formLanguages.toJS();
    if(validateParsedFile(data)){
      // checking if there is additional languages inside CSV
      if(data[1].length - 2 > languages.length){
        for(let i = 2; i < data[1].length; i++)
          if(!languages.includes(data[1][i])){
            props.addLanguage(data[1][i]);
          }
      }
      // translating
      for (let i = 2; i < data.length; i++) {
        for(let j = 2; j < data[i].length; j++){
          if(data[i] !== [] && data[i][0])
            updateTranslation(data[i][0], data[1][j], data[i][j].trim());
        }
      }
    }else{
      alert("CSV validation error, check the header");
    }
  };

  const getItemTranslations = () => {
    let translations = {};
    let metadata = {key : {}};
    props.items.forEach((v, k) => {
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

  const getValueSetTranslations = () => {
    if (props.valueSets) {
      let translations = {};
      let metadata = {key : {}};
      props.valueSets.forEach((vs, k) => {
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

  const createTranslationCSVformat = (itemTranslations,valueSetTranslations,result) => {
    for(let [key, value] of Object.entries(itemTranslations.metadata.key)){
      let row = []
      row.push(key)
      row.push(`${value.description} for ${key.split(":")[1]}`)
      props.formLanguages.forEach(l => {
        const name = itemTranslations.translations[key];
        row.push(name[l])
      })    
      result.push(row)
    }
    for(let [key, value] of Object.entries(valueSetTranslations.metadata.key)){
      let row = []
      row.push(key)
      row.push(`${value.description} for ${key.split(":")[3]}`)
      props.formLanguages.forEach(l => {
        const name = valueSetTranslations.translations[key];
        row.push(name[l])
      })    
      result.push(row)
    }
    return result
  }

  const downloadFormData = () => {
    let itemTranslations = getItemTranslations();
    let valueSetTranslations = getValueSetTranslations();
    let result = [];
    const firstRow = [props.formLabel]
    result.push(firstRow)
    const secondRow = ["Item ID", "Description"];
    props.formLanguages.forEach(l => {
      secondRow.push(l);
    })
    result.push(secondRow)

    itemTranslations.translations.
    result = createTranslationCSVformat(itemTranslations, valueSetTranslations, result)

    const csv = Papa.unparse(result);
    const blob = new Blob([csv], {type: 'text/csv'});
    FileSaver.saveAs(blob, `translation_${props.formLabel}.csv`);
  }

  if (props.translationOpen) {
    const newLanguages = Defaults.LANGUAGES
        .filter(lang => !props.formLanguages.contains(lang.code))
        .map(lang => ({key: lang.code, text: lang.name, value: lang.code}));

    const languages = {};
    props.formLanguages.forEach(l => {
      const lang = Defaults.LANGUAGES.find(c => c.code === l);
      languages[l] = {
        longName: lang.name || l,
        flag: lang.flag || l
      };
    })
    let itemTranslations = getItemTranslations();
    let valueSetTranslations = getValueSetTranslations();
    itemTranslations.metadata.language = languages;
    valueSetTranslations.metadata.language = languages;

    const panes = [
      {menuItem: 'Languages', render: () => <LanguageConfigurator
                                              languages={props.formLanguages}
                                              activeLanguage={props.language}
                                              newLanguages={newLanguages}
                                              addLanguage={props.addLanguage}
                                              setActiveLanguage={props.setActiveLanguage}
                                              deleteLanguage={props.deleteLanguage}
                                            />
      },
      {menuItem: 'Fields', render: () => <Translator
                                            key='fields'
                                            translations={itemTranslations.translations}
                                            metadata={itemTranslations.metadata}
                                            initialLanguage={props.language}
                                            onChange={updateTranslation}
                                            languages={props.formLanguages.toJS()} />
      },
      {menuItem: 'Lists', render: () =>  <Translator
                                            key='lists'
                                            translations={valueSetTranslations.translations}
                                            metadata={valueSetTranslations.metadata}
                                            initialLanguage={props.language}
                                            onChange={updateTranslation}
                                            languages={props.formLanguages.toJS()} />
      }
    ];

    return (
      <Modal open size='large' centered={false}>
        <Modal.Header>Translation</Modal.Header>
        <Modal.Content >
          <Tab panes={panes} />
        </Modal.Content>
        <Modal.Actions>
          <div style={{display: "flex", justifyContent: "space-between"}}>
            <div>
              <Button size='tiny' icon='download' onClick={() => downloadFormData()}/>
              <Button size='tiny' icon='upload' onClick={() => fileInputRef.current.click()} />
              <input
                ref={fileInputRef}
                type='file'
                accept='text/csv'
                hidden
                onChange={(e) => fileChange(e)}
              />
            </div>
            <Button primary onClick={() => props.hideTranslation()}>OK</Button>
          </div>
        </Modal.Actions>
      </Modal>
    );
  } else {
    return null;
  }
}

const TranslationDialogConnected = connect(
  state => ({
    translationOpen: state.dialobComposer.editor && state.dialobComposer.editor.get('translationOpen'),
    language: (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE,
    formLanguages: state.dialobComposer.form.getIn(['metadata', 'languages']),
    items: state.dialobComposer.form && state.dialobComposer.form.get('data'),
    formLabel: state.dialobComposer.form.getIn(['metadata', 'label']),
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
