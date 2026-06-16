import React from 'react';
import { FsDirentFlowTaskCreate, FsDirentFlowTaskUpdate, FsHelpFlowTask, FsPropertiesFlowTask } from '../fs-dirent-flow-task';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './index';

export const FlowTaskWidget: DirentWidget = {
  views: {
    CreateView: FsDirentFlowTaskCreate,
    UpdateView: FsDirentFlowTaskUpdate,
    PropertiesView: FsPropertiesFlowTask,
    HelpView: FsHelpFlowTask
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
    supportedViews: ['properties', 'references', 'debug', 'errors', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};


function _flow_task_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.FlowTask} {...props} />;
}
