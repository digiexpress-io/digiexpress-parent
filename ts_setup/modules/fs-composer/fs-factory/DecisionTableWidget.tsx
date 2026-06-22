import React from 'react';
import { FsDirentDecisionTableCreate, FsDirentDecisionTableUpdate, FsHelpDecisionTable } from '../fs-dirent-decision-table';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './index';

const FsPropertiesDecisionTable: React.FC = () => {
  return (<>TODO</>);
}

export const DecisionTableWidget: DirentWidget = {
  views: {
    CreateView: FsDirentDecisionTableCreate,
    UpdateView: FsDirentDecisionTableUpdate,
    PropertiesView: FsPropertiesDecisionTable,
    HelpView: FsHelpDecisionTable
  },
  icons: {
    dirent: {
      Marker: _decision_table_icon,
      Collapsed: _decision_table_icon,
      Expanded: _decision_table_icon
    }
  },
  colors: {
    dirent: FsColors.direntTypes.light.service
  },
  classNames: {
    dirent: '',
    icon: 'iconDecisionTable'
  },
  meta: {
    type: 'DECISION_TABLE',
    extension: '.decision-table',
    configOptions: [],
    supportedViews: ['properties', 'references', 'errors', 'history', 'changes', 'article-order', 'article-locale-overview', 'debug', 'stats', 'help'],
  }
};


function _decision_table_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.DecisionTable} {...props} />;
}
