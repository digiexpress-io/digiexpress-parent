import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './index';
import { FsDirentLocaleUpdate, FsDirentLocaleCreate } from '../fs-dirent-locale';
import { FsPropertiesLanguage } from '../fs-panel-properties/FsPropertiesLanguage';



export const LocaleWidget: DirentWidget = {
  views: {
    CreateView: FsDirentLocaleCreate,
    UpdateView: FsDirentLocaleUpdate,
    PropertiesView: FsPropertiesLanguage,
    HelpView: _locale_help_view
  },
  icons: {
    dirent: {
      Marker: _locale_icon,
      Collapsed: _locale_icon,
      Expanded: _locale_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.language,
    direntLight: FsColors.direntTypes.light.language
  },
  classNames: {
    dirent: '',
    icon: 'iconLanguage'
  },
  meta: {
    type: 'LOCALE',
    extension: '.locale',
    configOptions: ['DISABLED_MODE'],
    supportedViews: ['properties', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};

function _locale_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _locale_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Language} {...props} />;
}
