import { FsDirentPrintoutResourceCreate, FsDirentPrintoutResourceUpdate, FsPropertiesPrintoutResource, FsHelpPrintoutResource } from '../fs-dirent-printout-resource';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';

export const PrintoutResourceWidget: DirentWidget = {
  views: {
    CreateView: FsDirentPrintoutResourceCreate,
    UpdateView: FsDirentPrintoutResourceUpdate,
    PropertiesView: FsPropertiesPrintoutResource,
    HelpView: FsHelpPrintoutResource
  },
  icons: {
    dirent: {
      Marker: _printout_resource_icon,
      Collapsed: _printout_resource_icon,
      Expanded: _printout_resource_icon
    }
  },
  colors: {
    dirent: FsColors.direntTypes.light.asset
  },
  classNames: {
    dirent: '',
    icon: 'iconPrintoutResource'
  },
  meta: {
    type: 'PRINTOUT_RESOURCE',
    extension: '.printout-resource',
    configOptions: [],
    supportedViews: ['properties', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};


function _printout_resource_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Image} {...props} />;
}
