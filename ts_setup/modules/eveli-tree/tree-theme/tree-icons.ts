/**
 * Centralized icon declarations for EveliTree components
 * All Material-UI icons used across the tree component system
 * Usage: <TreeIcons.IconName>
 */
import {
  // Navigation & Expansion Icons
  ExpandMore,
  ChevronRight,
  UnfoldLess,
  UnfoldMore,
  UnfoldLessOutlined,

  // Theme & Mode Icons
  LightModeOutlined,
  DarkModeOutlined,

  // File & Folder Icons
  InsertDriveFileOutlined,
  FolderOutlined,
  Folder,
  FolderOpen,

  // Document Type Icons
  Article,
  ArticleOutlined,
  Description,
  PrintOutlined,
  ImageOutlined,
  PictureAsPdf,

  // Settings & Configuration Icons
  Settings,
  SettingsOutlined,

  // Action Icons
  Add,
  Edit,
  ContentCopy,
  DriveFileRenameOutline,
  DeleteForever,

  // Security & Access Icons
  Lock,
  LockOpen,

  // Configuration Mode Icons
  Construction,
  Assignment,
  Block,
  VisibilityOff,

  // Utility Icons
  Search,
  Close,
  Link,
  Language,
  Build,
  Error,
  BugReport,
  Visibility,
  Info,
  AccountTree,
  WorkHistory,
} from '@mui/icons-material';

export const TreeIcons = {
  // Navigation & Expansion
  ExpandMore: ExpandMore,
  ChevronRight: ChevronRight,
  CollapseAll: UnfoldLess,
  ExpandAll: UnfoldMore,

  // Theme & Mode
  LightMode: LightModeOutlined,
  DarkMode: DarkModeOutlined,

  // File & Folder
  File: InsertDriveFileOutlined,
  Folder: FolderOutlined,
  FolderClosed: Folder,
  FolderOpen: FolderOpen,

  // Document Types
  Article: Article,
  ArticleOutlined: ArticleOutlined,
  Form: Description,
  Print: PrintOutlined,
  Image: ImageOutlined,
  Pdf: PictureAsPdf,

  // Settings & Configuration
  Settings: Settings,
  SettingsOutlined: SettingsOutlined,

  // Actions
  Add: Add,
  New: Add,
  Edit: Edit,
  Copy: ContentCopy,
  Rename: DriveFileRenameOutline,
  Delete: DeleteForever,

  // Security & Access
  Locked: Lock,
  Unlocked: LockOpen,

  // Configuration Modes
  DevMode: Construction,
  Assignment: Assignment,
  Disabled: Block,
  Anonymous: VisibilityOff,

  // Utility
  Search: Search,
  Close: Close,
  Link: Link,
  Language: Language,
  Flow: Build,
  Error: Error,
  Debug: BugReport,
  Preview: Visibility,
  Info: Info,
  Tree: AccountTree,
  History: WorkHistory,
};