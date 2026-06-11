import React from 'react';
import { FsDirentDecisionTableCreate, FsDirentDecisionTableUpdate } from '../fs-dirent-decision-table';
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
    HelpView: _decision_table_help_view
  },
  icons: {
    dirent: {
      Marker: _decision_table_icon,
      Collapsed: _decision_table_icon,
      Expanded: _decision_table_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.service,
    direntLight: FsColors.direntTypes.light.service
  },
  classNames: {
    dirent: '',
    icon: 'iconDecisionTable'
  },
  meta: {
    type: 'DECISION_TABLE',
    extension: '.decision-table',
    configOptions: [],
  }
};

function _decision_table_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _decision_table_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.DecisionTable} {...props} />;
}
