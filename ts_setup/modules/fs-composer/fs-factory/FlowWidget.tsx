import React from 'react';
import { FsDirentFlowCreate } from '../fs-dirent-flow/FsDirentFlowCreate';
import { FsDirentFlowUpdate } from '../fs-dirent-flow/FsDirentFlowUpdate';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './index';

const FsPropertiesFlow: React.FC = () => {
  return (<>TODO</>);
}

export const FlowWidget: DirentWidget = {
  views: {
    CreateView: FsDirentFlowCreate,
    UpdateView: FsDirentFlowUpdate,
    PropertiesView: FsPropertiesFlow,
    HelpView: _flow_help_view
  },
  icons: {
    dirent: {
      Marker: _flow_icon,
      Collapsed: _flow_icon,
      Expanded: _flow_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.flow,
    direntLight: FsColors.direntTypes.light.flow
  },
  classNames: {
    dirent: '',
    icon: 'iconFlow'
  },
  meta: {
    type: 'FLOW',
    extension: '.flow',
    configOptions: ['DEV_MODE', 'DISABLED_MODE'],
    supportedViews: ['properties', 'references', 'debug', 'errors', 'preview', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};


function _flow_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _flow_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Flow} {...props} />;
}
