import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { FsDirentArticleCreate, FsDirentArticleUpdate, FsPropertiesArticle, FsHelpArticle } from '../fs-dirent-article';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';



export const ArticleWidget: DirentWidget = {
  views: {
    CreateView: FsDirentArticleCreate,
    UpdateView: FsDirentArticleUpdate,
    PropertiesView: FsPropertiesArticle,
    HelpView: FsHelpArticle
  },
  icons: {
    dirent: {
      Marker: _article_icon,
      Collapsed: _article_icon,
      Expanded: _article_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.article,
    direntLight: FsColors.direntTypes.light.article
  },
  classNames: {
    dirent: '',
    icon: 'iconArticle'
  },
  meta: {
    type: 'ARTICLE',
    extension: '.article',
    configOptions: ['DEV_MODE', 'DISABLED_MODE'],
    supportedViews: ['properties', 'references', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};
function _article_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Article} {...props} />;
}
