import { FsDirentFolderCreate, FsDirentFolderUpdate } from '../fs-dirent-folder';
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
    HelpView: _folder_help_view
  },
  icons: {
    dirent: {
      Marker: _folder_collapsed_icon,
      Collapsed: _folder_collapsed_icon,
      Expanded: _folder_expanded_icon
    }
  },
  colors: {
    direntDark: FsColors.direntTypes.dark.folder,
    direntLight: FsColors.direntTypes.light.folder
  },
  classNames: {
    dirent: '',
    icon: 'iconFolder'
  },
  meta: {
    type: 'FOLDER',
    extension: '', // left empty to prevent .folder extension visible on every UI folder
    configOptions: [],
  }
};

// TODO
function _folder_help_view() {
  return (<>HELP MARKDOWN</>);
}
function _folder_expanded_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.FolderOpen} {...props} />;
}
function _folder_collapsed_icon(props: DirentWidgetIconProps) {
  return <FsIcon icon={FsIcons.FolderClosed} {...props} />;
}
