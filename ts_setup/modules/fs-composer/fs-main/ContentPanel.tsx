import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsPanel } from '../fs-panel';

import { FsArticleOrder } from '../fs-article-order';
import { FsChanges } from '../fs-changes';
import { FsConfigOptions } from '../fs-config-options';
import { FsErrors } from '../fs-errors';
import { FsHelp } from '../fs-help';
import { FsHistory } from '../fs-history';
import { FsProperties } from '../fs-properties';
import { FsReferences } from '../fs-references';

import { OwnerState } from './useOwnerState';

export interface ContentPanelProps {
  ownerState: OwnerState;
  className: string;
}

export const ContentPanel: React.FC<ContentPanelProps> = ({ ownerState, className }) => {
  const intl = useIntl();
  const { activeDirent, selectedView } = ownerState;

  return (
    <div className={className}>
      {!selectedView ? (
        <FsPanel title={intl.formatMessage({ id: 'fs.main.chooseView.title' })}>
          <Typography>{intl.formatMessage({ id: 'fs.main.chooseView.message' })}</Typography>
        </FsPanel>
      ) : selectedView === 'changes' ? (
          <FsChanges dirent={activeDirent} />
        ) : (() => {
          switch (selectedView) {
            case 'errors':
              return <FsErrors dirent={activeDirent} />;
            case 'references':
              return <FsReferences dirent={activeDirent} />;
          case 'properties':
              return <FsProperties dirent={activeDirent} />;
          case 'history':
              return <FsHistory dirent={activeDirent} />;
          case 'help':
              return <FsHelp dirent={activeDirent} />;
          case 'configuration':
              return <FsConfigOptions dirent={activeDirent} />;
          case 'article-order':
              return <FsArticleOrder />;
          default:
            return (
              <FsPanel title='View not implemented'>
                <Typography>
                  The "{selectedView}" view is not yet implemented.
                </Typography>
              </FsPanel>
            );
        }
      })()}
    </div>
  );
};


