import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';
import { FsDirentArticlePageCreate, FsDirentArticlePageUpdate } from '../fs-dirent-article-page';
import { FsPropertiesArticlePage } from '../fs-panel-properties/FsPropertiesArticlePage';


export const ArticlePageWidget: DirentWidget = {
  views: {
    CreateView: FsDirentArticlePageCreate,
    UpdateView: FsDirentArticlePageUpdate,
    PropertiesView: FsPropertiesArticlePage,
    HelpView: _article_help_view
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
  }
};

// TODO
function _article_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _article_page_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Page} {...props} />;
}
