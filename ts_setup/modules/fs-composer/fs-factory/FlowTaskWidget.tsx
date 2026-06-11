import React from 'react';
import { FsDirentFlowTaskCreate, FsDirentFlowTaskUpdate } from '../fs-dirent-flow-task';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './index';

const FsPropertiesFlowTask: React.FC = () => {
  return (<>TODO</>);
}

export const FlowTaskWidget: DirentWidget = {
  views: {
    CreateView: FsDirentFlowTaskCreate,
    UpdateView: FsDirentFlowTaskUpdate,
    PropertiesView: FsPropertiesFlowTask,
    HelpView: _flow_task_help_view
  },
  icons: {
    dirent: {
      Marker: _flow_task_icon,
      Collapsed: _flow_task_icon,
      Expanded: _flow_task_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.flow,
    direntLight: FsColors.direntTypes.light.flow
  },
  classNames: {
    dirent: '',
    icon: 'iconFlowTask'
  },
  meta: {
    type: 'FLOW_TASK',
    extension: '.flow-task',
    configOptions: [],
  }
};

function _flow_task_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _flow_task_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.FlowTask} {...props} />;
}
