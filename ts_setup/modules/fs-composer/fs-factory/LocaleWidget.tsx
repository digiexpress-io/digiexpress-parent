import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';
import { FsDirentLocaleUpdate, FsDirentLocaleCreate, FsPropertiesLocale, FsHelpLocale } from '../fs-dirent-locale';



export const LocaleWidget: DirentWidget = {
  views: {
    CreateView: FsDirentLocaleCreate,
    UpdateView: FsDirentLocaleUpdate,
    PropertiesView: FsPropertiesLocale,
    HelpView: FsHelpLocale
  },
  icons: {
    dirent: {
      Marker: _locale_icon,
      Collapsed: _locale_icon,
      Expanded: _locale_icon
    }
  },
  colors: {
    dirent: FsColors.direntTypes.language
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


function _locale_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Language} {...props} />;
}
