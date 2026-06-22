import { FsDirentPrintoutCreate, FsDirentPrintoutUpdate, FsPropertiesPrintout, FsHelpPrintout } from '../fs-dirent-printout';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';

export const PrintoutWidget: DirentWidget = {
  views: {
    CreateView: FsDirentPrintoutCreate,
    UpdateView: FsDirentPrintoutUpdate,
    PropertiesView: FsPropertiesPrintout,
    HelpView: FsHelpPrintout
  },
  icons: {
    dirent: {
      Marker: _printout_icon,
      Collapsed: _printout_icon,
      Expanded: _printout_icon
    }
  },
  colors: {
    dirent: FsColors.direntTypes.printout
  },
  classNames: {
    dirent: '',
    icon: 'iconPrintout'
  },
  meta: {
    type: 'PRINTOUT',
    extension: '.printout',
    configOptions: [],
    supportedViews: ['properties', 'references', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};


function _printout_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Print} {...props} />;
}
