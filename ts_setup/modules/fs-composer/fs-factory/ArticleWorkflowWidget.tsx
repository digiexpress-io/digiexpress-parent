import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';
import { FsPropertiesWorkflow } from '../fs-panel-properties/FsPropertiesWorkflow';
import { FsDirentArticleWorkflowCreate, FsDirentArticleWorkflowUpdate } from '../fs-dirent-article-workflow';



export const ArticleWorkflowWidget: DirentWidget = {
  views: {
    CreateView: FsDirentArticleWorkflowCreate,
    UpdateView: FsDirentArticleWorkflowUpdate,
    PropertiesView: FsPropertiesWorkflow,
    HelpView: _article_workflow_help_view
  },
  icons: {
    dirent: {
      Marker: _article_workflow_icon,
      Collapsed: _article_workflow_icon,
      Expanded: _article_workflow_icon
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
    type: 'ARTICLE_WORKFLOW',
    extension: '.article-workflow',
    configOptions: ['DEV_MODE', 'DISABLED_MODE'],
    supportedViews: ['properties', 'references', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};

// TODO
function _article_workflow_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _article_workflow_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Settings} {...props} />;
}
