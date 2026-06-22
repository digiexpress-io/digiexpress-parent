import { FsDirentFolderCreate, FsDirentFolderUpdate, FsHelpFolder } from '../fs-dirent-folder';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { DirentWidget, DirentWidgetIconProps } from './WidgetFactory';


const FsPropertiesFolder: React.FC = () => {
  return (<>TODO</>)
}

export const FolderWidget: DirentWidget = {
  views: {
    CreateView: FsDirentFolderCreate,
    UpdateView: FsDirentFolderUpdate,
    PropertiesView: FsPropertiesFolder,
    HelpView: FsHelpFolder
  },
  icons: {
    dirent: {
      Marker: _folder_collapsed_icon,
      Collapsed: _folder_collapsed_icon,
      Expanded: _folder_expanded_icon
    }
  },
  colors: {
    dirent: FsColors.direntTypes.folder
  },
  classNames: {
    dirent: '',
    icon: 'iconFolder'
  },
  meta: {
    type: 'FOLDER',
    extension: '', // left empty to prevent .folder extension visible on every UI folder
    configOptions: [],
    supportedViews: ['properties', 'changes', 'article-order', 'article-locale-overview', 'stats', 'help'],
  }
};
function _folder_expanded_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.FolderOpen} {...props} />;
}
function _folder_collapsed_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.FolderClosed} {...props} />;
}
