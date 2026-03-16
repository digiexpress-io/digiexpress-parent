import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsPanel } from '../fs-panel';

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
  const { activeNode, selectedView } = ownerState;

  return (
    <div className={className}>
      {!selectedView ? (
        <FsPanel title={intl.formatMessage({ id: 'fs.main.chooseView.title' })}>
          <Typography>{intl.formatMessage({ id: 'fs.main.chooseView.message' })}</Typography>
        </FsPanel>
      ) : selectedView === 'changes' ? (
          <FsChanges node={activeNode} />
        ) : (() => {
          switch (selectedView) {
            case 'errors':
              return <FsErrors node={activeNode} />;
            case 'references':
            return <FsReferences node={activeNode} />;
          case 'properties':
            return <FsProperties node={activeNode} />;
          case 'history':
            return <FsHistory node={activeNode} />;
          case 'help':
            return <FsHelp node={activeNode} />;
          case 'configuration':
            return <FsConfigOptions node={activeNode} />;
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


