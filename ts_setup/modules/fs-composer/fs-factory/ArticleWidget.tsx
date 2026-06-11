import { FsDirentArticleCreate, FsDirentArticleUpdate } from '../fs-dirent-article';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';
import { FsPropertiesLink } from '../fs-panel-properties/FsPropertiesLink';



export const ArticleWidget: DirentWidget = {
  views: {
    CreateView: FsDirentArticleCreate,
    UpdateView: FsDirentArticleUpdate,
    PropertiesView: FsPropertiesLink,
    HelpView: _article_help_view
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

// TODO
function _article_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _article_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Article} {...props} />;
}
