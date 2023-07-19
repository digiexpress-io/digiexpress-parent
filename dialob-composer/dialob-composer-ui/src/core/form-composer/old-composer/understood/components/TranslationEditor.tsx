//@ts-nocheck
import produce from 'immer';
import React from 'react';
import { Button, Divider, Flag, Form, Header, Segment, TextArea } from 'semantic-ui-react';
import { KeyMeta, LangMeta } from '../types';

export interface EditorProps {
  value: string;
  onChange: (value: string) => void;
  metadata: KeyMeta | undefined;
  translationKey: string;
  language: string;
}

const DefaultEditor: React.FC<EditorProps> = ({ value, onChange }) => {
  return (
    <TextArea value={value} onChange={e => onChange(e.currentTarget.value)}/>
  );
}

interface Props {
  translationKey: string;
  languageValues: [string, string][];
  onChange: (language: string, value: string) => void;
  onMetaChange?: (metadata: KeyMeta) => void;
  keyMeta?: KeyMeta;
  langMeta?: { [language: string]: LangMeta };
  Editor?: React.ComponentType<EditorProps>;
};
export const TranslationEditor: React.FC<Props> = ({
  translationKey,
  languageValues,
  onChange,
  onMetaChange,
  keyMeta,
  langMeta,
  Editor = DefaultEditor
}) => {
  const needsWork = keyMeta && keyMeta.needsWork;
  return (
    <Segment className='editor-info'>
      {languageValues.map(([language, value]) => {
        const meta = langMeta && langMeta[language];
        return (
          <div key={language}>
            <Header size='medium'>
              {meta && meta.flag && <Flag name={meta.flag}/>}
              {(meta && meta.longName) || language}
            </Header>
            <Form>
              <Editor
                key={translationKey /* if editor uses state, we want to re-mount it between key changes */}
                translationKey={translationKey}
                language={language}
                value={value}
                onChange={value => onChange(language, value)}
                metadata={keyMeta}
              />
            </Form>
            <Divider/>
          </div>
        );
      })}
      {onMetaChange && (
        <Button color={needsWork ? 'green' : 'yellow'} onClick={e => {
          onMetaChange(produce(keyMeta || {}, metadata => {
            metadata.needsWork = !metadata.needsWork;
          }));
        }}>
          {needsWork ? 'Mark as OK' : 'Needs work'}
        </Button>
      )}
    </Segment>
  );
}
