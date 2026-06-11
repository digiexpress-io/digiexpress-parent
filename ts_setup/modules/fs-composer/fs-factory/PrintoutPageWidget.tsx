import React from 'react';
import { FsDirentPrintoutPageCreate, FsDirentPrintoutPageUpdate } from '../fs-dirent-printout-page';
import { FsPropertiesPrintoutPage } from '../fs-panel-properties/FsPropertiesPrintoutPage';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './index';

export const PrintoutPageWidget: DirentWidget = {
  views: {
    CreateView: FsDirentPrintoutPageCreate,
    UpdateView: FsDirentPrintoutPageUpdate,
    PropertiesView: FsPropertiesPrintoutPage,
    HelpView: _printout_page_help_view
  },
  icons: {
    dirent: {
      Marker: _printout_page_icon,
      Collapsed: _printout_page_icon,
      Expanded: _printout_page_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.page,
    direntLight: FsColors.direntTypes.light.page
  },
  classNames: {
    dirent: '',
    icon: 'iconPrintoutPage'
  },
  meta: {
    type: 'PRINTOUT_PAGE',
    extension: '.printout-page',
    configOptions: [],
  }
};

function _printout_page_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _printout_page_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Page} {...props} />;
}
