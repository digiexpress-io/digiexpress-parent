import produce from 'immer';
import React, { useCallback, useMemo, useState, useRef } from 'react';
import { AutoSizer, Column as VirtualizedColumn, Table as VirtualizedTable, TableCellProps } from 'react-virtualized';
import 'react-virtualized/styles.css';
import { Button, Dropdown, Flag, Icon, Input, Message, Segment, Table } from 'semantic-ui-react';
import '../css/table-editor.css';
import { createFilterKeys, createIsProblemKey, parseAvailableLanguages, parseTranslations, pickLanguageValues, ProblemKeyOpts } from '../helpers';
import { useStatePreferProp } from '../hooks/useStatePreferProp';
import { Format, Key, Language, Metadata, Translations } from '../types';
import Translation, { TranslationCellProps } from './Translation';
import { EditorProps, TranslationEditor } from './TranslationEditor';

export interface TableEditorProps {
  initialTranslations: Translations;
  initialMetadata?: Metadata;
  format: Format;
  availableLanguages?: Language[];
  initialLanguages?: Language[];
  onChange?: (translations: Translations) => void;
  onChangeItem?: (key: Key, language: Language, text: string) => void;
  onMetadataChange?: (metadata: Metadata) => void;
  plaintextTranslation?: (translation: string) => string;
  components?: {
    Editor?: React.ComponentType<EditorProps>;
    TranslationCell?: React.ComponentType<TranslationCellProps>;
  }
};

export const TableEditor: React.FC<TableEditorProps> = (props) => {
  const {
    components,
    initialLanguages,
    initialTranslations,
    initialMetadata,
    onMetadataChange,
  } = props;

  const [availableLanguages, setAvailableLanguages] = useStatePreferProp(() => {
    return parseAvailableLanguages(initialTranslations, props.format);
  }, props.availableLanguages);
  
  const [shownLanguages, setShownLanguages] = useState(() => {
    if(!initialLanguages || initialLanguages.length === 0) {
      return availableLanguages.slice(0, 2);
    }
    return initialLanguages;
  });

  const plaintextCache = useRef<{ [t: string]: string }>({});
  const plaintextTranslation = useCallback((value: string) => {
    if(!props.plaintextTranslation) return value;
    if(plaintextCache.current) {
      if(plaintextCache.current[value]) {
        return plaintextCache.current[value];
      }
      const plaintext = props.plaintextTranslation(value);
      plaintextCache.current[value] = plaintext;
      return plaintext;
    }
    return props.plaintextTranslation(value);
  }, [props.plaintextTranslation]);

  const [translations, setTranslations] = useState<Translations>(() => parseTranslations(initialTranslations, props.format));
  const [prevTranslations, setPrevTranslations] = useState<Translations>(initialTranslations);
  const [externalTranslations, setExternalTranslations] = useState(initialTranslations);
  const [filterText, setFilterText] = useState('');
  const [metadata, setMetadata] = useState(initialMetadata);
  const [prevMetadata, setPrevMetadata] = useState(initialMetadata);
  const filterKeys = createFilterKeys(translations, filterText, plaintextTranslation);
  const [shownKeys, setShownKeys] = useState(() => filterKeys());
  const [activeKey, setActiveKey] = useState(shownKeys[0]);

  const isProblemKey = createIsProblemKey(translations, availableLanguages, metadata);
  const findProblemKeys = (opts?: ProblemKeyOpts & { shownKeys?: string[] }) => {
    const keys = (opts && opts.shownKeys) || shownKeys;
    return new Set(keys.filter(key => isProblemKey(key, opts)))
  };
  const [problemKeys, setProblemKeys] = useState(() => findProblemKeys());
  const updateProblemKey = (key: string, opts?: ProblemKeyOpts) => {
    if(isProblemKey(key, opts)) {
      problemKeys.add(activeKey);
      setProblemKeys(problemKeys);
    } else {
      problemKeys.delete(activeKey);
      setProblemKeys(problemKeys);
    }
  }

  const updateShownKeys = (newKeys: string[]) => {
    setShownKeys(newKeys);
    setProblemKeys(findProblemKeys({ shownKeys: newKeys }));

    if(!newKeys.includes(activeKey)) {
      setActiveKey(newKeys[0]);
    }
  }

  if(prevTranslations !== initialTranslations && externalTranslations !== initialTranslations) {
    const newTranslations = parseTranslations(initialTranslations, props.format);
    const newAvailableLanguages = parseAvailableLanguages(initialTranslations, props.format);
    setTranslations(newTranslations);
    setPrevTranslations(initialTranslations);
    setAvailableLanguages(newAvailableLanguages);
    if(prevMetadata !== initialMetadata && metadata !== initialMetadata) {
      setMetadata(initialMetadata);
      setPrevMetadata(initialMetadata);
      setProblemKeys(findProblemKeys({ translations: newTranslations, languages: newAvailableLanguages, metadata: initialMetadata }));
    } else {
      setProblemKeys(findProblemKeys({ translations: newTranslations, languages: newAvailableLanguages }));
    }
    setExternalTranslations(initialTranslations);
  } else if(prevMetadata !== initialMetadata && metadata !== initialMetadata) {
    setMetadata(initialMetadata);
    setPrevMetadata(initialMetadata);
    setProblemKeys(findProblemKeys({ metadata: initialMetadata }));
  }

  const isKeyColumnVisible = shownLanguages[0] === '$key';
  const shownLen = shownLanguages.length - (isKeyColumnVisible ? 1 : 0);
  const minAmntOfLanguages = initialLanguages ? initialLanguages.length : 2;
  const minAmntOfVisibleLanguages = shownLen <= minAmntOfLanguages;

  const hiddenLanguages = useMemo(() => {
    return availableLanguages.filter(lang => !shownLanguages.includes(lang));
  }, [availableLanguages, shownLanguages]);

  const createLangOption = useCallback((language: Language) => {
    const langMeta = metadata && metadata.language && metadata.language[language];
    if(!langMeta) {
      return { text: language, value: language };
    }

    return {
      text: (
        <span>
          {langMeta.flag && <Flag name={langMeta.flag}/>}
          {langMeta.longName || language}
        </span>
      ),
      value: language,
    };
  }, [metadata]);

  const hiddenLanguageOptions = useMemo(() => {
    return hiddenLanguages.map(createLangOption);
  }, [hiddenLanguages, createLangOption]);

  const headers = useMemo(() => {
    return shownLanguages.map(language => {
      if(language === '$key') {
        return (
          <Table.HeaderCell key={language}>
            key
            <Icon
              className='remove-icon'
              name='remove'
              style={{float: 'right'}}
              onClick={() => {
                setShownLanguages(shownLanguages.slice(1));
              }}
            />
          </Table.HeaderCell>
        );
      }
  
      return (
        <Table.HeaderCell key={language}>
          <Dropdown
            inline
            value={language}
            options={[...hiddenLanguageOptions, createLangOption(language)]}
            onChange={(_event, data) => {
              setShownLanguages(produce(shownLanguages, shownLanguages => {
                const index = shownLanguages.indexOf(language); 
                shownLanguages[index] = data.value as string;
              }));
            }}
          />
          <Icon
            className='remove-icon'
            name='remove'
            style={{float: 'right'}}
            onClick={() => {
              if(!minAmntOfVisibleLanguages) {
                setShownLanguages(shownLanguages.filter(shownLang => shownLang != language));
              }
            }}
          />
        </Table.HeaderCell>
      );
    });
  }, [shownLanguages, hiddenLanguageOptions, minAmntOfVisibleLanguages]);

  const onChangeEditor = (language: Language, value: string) => {
    setPrevTranslations(initialTranslations);
    const newTranslations = produce(translations, translations => {
      translations[activeKey][language] = value;
    });
    setTranslations(newTranslations);

    let newExternalTranslations: Translations;
    if(props.format === 'languageToKey') {
      newExternalTranslations = produce(externalTranslations, externalTranslations => {
        externalTranslations[language][activeKey] = value;
      });
    } else if(props.format === 'keyToLanguage') {
      newExternalTranslations = newTranslations;
    } else {
      throw new Error('Unrecognized translation format!');
    }

    setExternalTranslations(newExternalTranslations);
    updateProblemKey(activeKey, { translations: newTranslations });

    if(plaintextCache.current) {
      const prevValue = translations[activeKey][language];
      if(prevValue !== undefined) {
        delete plaintextCache.current[prevValue];
      }
    }

    if(props.onChange) {
      props.onChange(newExternalTranslations);
    }

    if(props.onChangeItem) {
      props.onChangeItem(activeKey, language, value);
    }
  }

  const rowGetter = ({ index }: { index: number }) => {
    const key = shownKeys[index];
    if(!translations[key]) return {};
    return translations[key];
  }

  const virtualizedTableRef = useRef<VirtualizedTable>(null);
  const focusNextProblemKey = () => {
    if(problemKeys.size === 0 || !virtualizedTableRef.current) return;
    const problemIndexes: number[] = [];
    let activeIdx: number = -1;
    shownKeys.forEach((key, idx) => {
      if(key === activeKey) {
        activeIdx = idx;
        return;
      }

      if(problemKeys.has(key)) {
        problemIndexes.push(idx);
      }
    });
    if(problemIndexes.length === 0) return;

    let nextIdx = problemIndexes[0];
    if(activeIdx !== -1) {
      const distance = (idx: number) => {
        let d = idx - activeIdx;
        if(d < 0) d += shownKeys.length;
        return d;
      };
      problemIndexes.forEach(idx => {
        if(distance(idx) < distance(nextIdx)) {
          nextIdx = idx;
        }
      });
    }

    virtualizedTableRef.current.scrollToRow(nextIdx);
    setActiveKey(shownKeys[nextIdx]);
  }

  const tableColumns = useMemo(() => {
    const cellRenderer = ({ cellData = '', rowIndex, dataKey: language }: TableCellProps, isKey?: boolean) => {
      const key = shownKeys[rowIndex];

      return (
        <Translation
          value={cellData}
          isKey={isKey}
          active={activeKey === key}
          metadata={metadata && metadata.key && metadata.key[key]}
          onClick={() => setActiveKey(key)}
          CellComponent={components && components.TranslationCell}
          plaintextTranslation={plaintextTranslation}
        />
      );
    }

    const keyCellRenderer = ({ rowIndex, ...restProps }: TableCellProps) => {
      const key = shownKeys[rowIndex];
      return cellRenderer({ ...restProps, dataKey: key, cellData: key, rowIndex }, true);
    }

    return shownLanguages.map((language, idx) => {
      if(language === '$key') {
        return <VirtualizedColumn
          key='$key'
          label='key'
          dataKey='key'
          cellRenderer={keyCellRenderer}
          width={180}
          flexGrow={1}
        />
      } else {
        return <VirtualizedColumn
          key={language}
          label={language}
          dataKey={language}
          cellRenderer={cellRenderer}
          width={180}
          flexGrow={1}
        />
      }
    });
  }, [shownLanguages, shownKeys, metadata, activeKey]);

  return (
    <div className='editor-container'>
      <div>
        <Table attached='top' className='editor-table-header'>
          <Table.Header>
            <Table.Row>
              {headers}
            </Table.Row>
          </Table.Header>
          <Table.Body/>
        </Table>
        <Segment attached className='editor-main'>
          <AutoSizer>
            {({ width, height }) => (
              <VirtualizedTable
                ref={virtualizedTableRef}
                className='editor-table'
                disableHeader
                overscanRowCount={5}
                width={width}
                height={height}
                rowClassName='editor-table-row'
                rowGetter={rowGetter}
                headerHeight={30}
                rowHeight={40}
                rowCount={shownKeys.length}
                noRowsRenderer={() => (
                  <div className='empty-message'>
                    <Message
                      info
                      header='No translations'
                      content='Found no translations with the given parameters'
                    />
                  </div>
                )}
              >
                { tableColumns }
              </VirtualizedTable>
            )}
          </AutoSizer>
        </Segment>
        <Segment attached='bottom' compact secondary className='editor-footer'>
          <Input
            placeholder='Search'
            icon='search'
            iconPosition='left'
            onClick={(e: React.MouseEvent<HTMLInputElement>) => {
              e.preventDefault();
              e.stopPropagation();
            }}
            value={filterText}
            onChange={(e, { value }) => {
              setFilterText(value);
              updateShownKeys(filterKeys({ filterText: value }));
            }}
          />
          <Button icon labelPosition='left' primary className='editor-problems-btn' onClick={focusNextProblemKey}>
            <Icon name='indent'/>
            {problemKeys.size} {problemKeys.size === 1 ? 'problem' : 'problems'}
          </Button>
          <Dropdown
            className='lang-select'
            placeholder='Show language'
            selection
            search
            options={isKeyColumnVisible
              ? hiddenLanguageOptions
              : [...hiddenLanguageOptions, { text: 'Show keys', content: <Button fluid size='mini' primary as='span'>Show keys</Button>, value: '$key' }]
            }
            onChange={(event: React.SyntheticEvent, data) => {
              if(data.value === '$key') {
                setShownLanguages([data.value, ...shownLanguages]);
              } else {
                setShownLanguages([...shownLanguages, data.value as string]);
              }
            }}
            selectOnBlur={false}
            value=''
          />
        </Segment>
      </div>
      {activeKey && (
        <TranslationEditor
          translationKey={activeKey}
          languageValues={pickLanguageValues(translations, shownLanguages, activeKey)}
          keyMeta={metadata && metadata.key && metadata.key[activeKey]}
          langMeta={metadata && metadata.language}
          onMetaChange={onMetadataChange && ((keymeta) => {
            const newMetadata = produce(metadata || {}, metadata => {
              if(!metadata.key) {
                metadata.key = {};
              }
              metadata.key[activeKey] = keymeta;
            });
            setMetadata(newMetadata);
            updateProblemKey(activeKey, { metadata: newMetadata });
            onMetadataChange(newMetadata);
          })}
          onChange={onChangeEditor}
          Editor={components && components.Editor}
        />
      )}
    </div>
  );
}
