
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';
import { FsDirentArticleLinkUpdate, FsDirentArticleLinkCreate, FsPropertiesLink, FsHelpArticleLink } from '../fs-dirent-article-link/';



export const ArticleLinkWidget: DirentWidget = {
  views: {
    CreateView: FsDirentArticleLinkCreate,
    UpdateView: FsDirentArticleLinkUpdate,
    PropertiesView: FsPropertiesLink,
    HelpView: FsHelpArticleLink
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
    supportedViews: ['properties', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};

function _link_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Link} {...props} />;
}