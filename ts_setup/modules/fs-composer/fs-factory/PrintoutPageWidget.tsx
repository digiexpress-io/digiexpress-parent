import { FsDirentPrintoutPageCreate, FsDirentPrintoutPageUpdate, FsPropertiesPrintoutPage, FsHelpPrintoutPage } from '../fs-dirent-printout-page';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';

export const PrintoutPageWidget: DirentWidget = {
  views: {
    CreateView: FsDirentPrintoutPageCreate,
    UpdateView: FsDirentPrintoutPageUpdate,
    PropertiesView: FsPropertiesPrintoutPage,
    HelpView: FsHelpPrintoutPage
  },
  icons: {
    dirent: {
      Marker: _printout_page_icon,
      Collapsed: _printout_page_icon,
      Expanded: _printout_page_icon
    }
  },
  colors: {
    dirent: FsColors.direntTypes.page
  },
  classNames: {
    dirent: '',
    icon: 'iconPrintoutPage'
  },
  meta: {
    type: 'PRINTOUT_PAGE',
    extension: '.printout-page',
    configOptions: [],
    supportedViews: ['properties', 'preview', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};


function _printout_page_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Page} {...props} />;
}
