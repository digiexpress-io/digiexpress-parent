
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';
import { FsDirentArticleLinkUpdate, FsDirentArticleLinkCreate } from '../fs-dirent-article-link/';
import { FsPropertiesLink } from '../fs-panel-properties/FsPropertiesLink';



export const ArticleLinkWidget: DirentWidget = {
  views: {
    CreateView: FsDirentArticleLinkCreate,
    UpdateView: FsDirentArticleLinkUpdate,
    PropertiesView: FsPropertiesLink,
    HelpView: _link_help_view
  },
  icons: {
    dirent: {
      Marker: _link_icon,
      Collapsed: _link_icon,
      Expanded: _link_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.link,
    direntLight: FsColors.direntTypes.light.link
  },
  classNames: {
    dirent: '',
    icon: 'iconLink'
  },
  meta: {
    type: 'ARTICLE_LINK',
    extension: '.link',
    configOptions: ['DEV_MODE', 'DISABLED_MODE'],
  }
};
function _link_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _link_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Link} {...props} />;
}