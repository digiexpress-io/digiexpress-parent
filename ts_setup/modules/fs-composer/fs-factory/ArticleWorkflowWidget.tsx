import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';
import { FsDirentArticleWorkflowCreate, FsDirentArticleWorkflowUpdate, FsPropertiesWorkflow, FsHelpArticleWorkflow } from '../fs-dirent-article-workflow';



export const ArticleWorkflowWidget: DirentWidget = {
  views: {
    CreateView: FsDirentArticleWorkflowCreate,
    UpdateView: FsDirentArticleWorkflowUpdate,
    PropertiesView: FsPropertiesWorkflow,
    HelpView: FsHelpArticleWorkflow
  },
  icons: {
    dirent: {
      Marker: _article_workflow_icon,
      Collapsed: _article_workflow_icon,
      Expanded: _article_workflow_icon
    }
  },
  colors: {
    dirent: FsColors.direntTypes.article
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
function _article_workflow_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.Settings} {...props} />;
}
