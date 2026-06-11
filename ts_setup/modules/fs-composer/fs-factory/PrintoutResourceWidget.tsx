import React from 'react';
import { FsDirentPrintoutResourceCreate, FsDirentPrintoutResourceUpdate } from '../fs-dirent-printout-resource';
import { FsPropertiesPrintoutResource } from '../fs-panel-properties/FsPropertiesPrintoutResource';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './index';

export const PrintoutResourceWidget: DirentWidget = {
  views: {
    CreateView: FsDirentPrintoutResourceCreate,
    UpdateView: FsDirentPrintoutResourceUpdate,
    PropertiesView: FsPropertiesPrintoutResource,
    HelpView: _printout_resource_help_view
  },
  icons: {
    dirent: {
      Marker: _printout_resource_icon,
      Collapsed: _printout_resource_icon,
      Expanded: _printout_resource_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.asset,
    direntLight: FsColors.direntTypes.light.asset
  },
  classNames: {
    dirent: '',
    icon: 'iconPrintoutResource'
  },
  meta: {
    type: 'PRINTOUT_RESOURCE',
    extension: '.printout-resource',
    configOptions: [],
  }
};

function _printout_resource_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _printout_resource_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Image} {...props} />;
}
