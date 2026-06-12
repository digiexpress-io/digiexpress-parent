import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';
import { FsDirentArticlePageCreate, FsDirentArticlePageUpdate, FsPropertiesArticlePage, FsHelpArticlePage } from '../fs-dirent-article-page';


export const ArticlePageWidget: DirentWidget = {
  views: {
    CreateView: FsDirentArticlePageCreate,
    UpdateView: FsDirentArticlePageUpdate,
    PropertiesView: FsPropertiesArticlePage,
    HelpView: FsHelpArticlePage
  },
  icons: {
    dirent: {
      Marker: _article_page_icon,
      Collapsed: _article_page_icon,
      Expanded: _article_page_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.page,
    direntLight: FsColors.direntTypes.light.page
  },
  classNames: {
    dirent: '',
    icon: 'iconArticlePage'
  },
  meta: {
    type: 'ARTICLE_PAGE',
    extension: '.article-page',
    configOptions: ['DEV_MODE', 'DISABLED_MODE'],
    supportedViews: ['properties', 'preview', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};
function _article_page_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Page} {...props} />;
}
