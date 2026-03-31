import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsPanel } from '../fs-panel';

import { FsPanelArticleOrder } from '../fs-panel-article-order';
import { FsPanelChanges } from '../fs-panel-changes';
import { FsPanelConfigOptions } from '../fs-panel-config-options';
import { FsPanelErrors } from '../fs-panel-errors';
import { FsPanelHelp } from '../fs-panel-help';
import { FsPanelHistory } from '../fs-panel-history';
import { FsPanelProperties } from '../fs-panel-properties';
import { FsPanelReferences } from '../fs-panel-references';

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
          <FsPanelChanges dirent={activeDirent} />
        ) : (() => {
          switch (selectedView) {
            case 'errors':
              return <FsPanelErrors dirent={activeDirent} />;
            case 'references':
              return <FsPanelReferences dirent={activeDirent} />;
          case 'properties':
              return <FsPanelProperties dirent={activeDirent} />;
          case 'history':
              return <FsPanelHistory dirent={activeDirent} />;
          case 'help':
              return <FsPanelHelp dirent={activeDirent} />;
          case 'configuration':
              return <FsPanelConfigOptions dirent={activeDirent} />;
          case 'article-order':
              return <FsPanelArticleOrder />;
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


