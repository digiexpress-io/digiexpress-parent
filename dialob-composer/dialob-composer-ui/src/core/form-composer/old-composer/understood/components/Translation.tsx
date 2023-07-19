//@ts-nocheck
import React from 'react';
import '../css/translation.css';
import { KeyMeta } from '../types';
import { Header } from 'semantic-ui-react';

export interface TranslationCellProps {
  value: string;
  metadata?: KeyMeta;
}

type Props = {
  value: string;
  active: boolean;
  isKey?: boolean;
  metadata?: KeyMeta;
  plaintextTranslation?: (value: string) => string;
  onClick?: () => void;
  CellComponent?: React.ComponentType<TranslationCellProps>;
};

class Translation extends React.PureComponent<Props> {
  render() {
    const {
      value,
      isKey,
      metadata,
      active,
      onClick,
      CellComponent,
      plaintextTranslation,
    } = this.props;

    let className = 'translation';
    if(value.length === 0) {
      className += ' error';
    } else if(metadata && metadata.needsWork) {
      className += ' warning';
    }

    if(isKey) {
      className += ' disabled';
    }

    if(active) {
      className += ' active';
    }

    if(isKey) {
      return (
        <div className={className} onClick={onClick}>
          <Header size='tiny'>
            {value}
            {metadata && metadata.description && (
              <Header.Subheader>
                {metadata.description}
              </Header.Subheader>
            )}
          </Header>
        </div>
      )
    }

    let contents: React.ReactNode = value;
    if(CellComponent) {
      contents = <CellComponent value={value} metadata={metadata} />
    } else if(plaintextTranslation) {
      contents = plaintextTranslation(value);
    }

    return (
      <div className={className} onClick={onClick}>
        {contents}
      </div>
    );
  }
}

export default Translation;
