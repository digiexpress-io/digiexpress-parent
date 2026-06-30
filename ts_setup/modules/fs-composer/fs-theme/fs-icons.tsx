import React from 'react';

import {
  // Navigation & Expansion Icons
  ExpandMore,
  ChevronRight,
  UnfoldLess,
  UnfoldMore,

  // Theme & Mode Icons
  LightModeOutlined,

  // File & Folder Icons
  InsertDriveFileOutlined,
  FolderOutlined,
  Folder,
  FolderOpen,

  // Document Type Icons
  ArticleOutlined,
  Description,
  PrintOutlined,
  Image,
  PictureAsPdf,

  // Settings & Configuration Icons
  Settings,
  SettingsOutlined,

  // Action Icons
  Add,
  Edit,
  Save,
  ContentCopy,
  DriveFileRenameOutline,
  DeleteForever,
  Undo,

  // Security & Access Icons
  Lock,
  LockOpen,

  // Configuration Mode Icons
  Construction,
  Assignment,
  Block,
  VisibilityOff,
  HomeWorkOutlined,

  // Utility Icons
  Search,
  Close,
  Link,
  Language,
  Error,
  Warning,
  ErrorOutline,
  BugReport,
  Visibility,
  Info,
  AccountTree,
  WorkHistory,
  Help,
  Phone,
  FormatListNumbered,
  Feed,
  MenuBook,
  SubdirectoryArrowRight,
  TableChart,

  // Decision Table Toolbar Icons
  DoubleArrowRounded,
  CompareArrowsRounded,
  FileDownloadDone,
  Upload,
  TaskAlt,

  // Stats & Checkmark
  Assessment,
  Check,

  // Unsaved indicator
  FiberManualRecord,
  LabelOutlined as LabelIcon,
  NotesOutlined,
  CodeOutlined,
} from '@mui/icons-material';
import { SvgIconProps, Tooltip } from '@mui/material';

export const FsIcons = {
  // Navigation & Expansion
  ExpandMore: ExpandMore,
  ChevronRight: ChevronRight,
  CollapseAll: UnfoldLess,
  ExpandAll: UnfoldMore,

  // Theme & Mode
  LightMode: LightModeOutlined,

  // File & Folder
  File: InsertDriveFileOutlined,
  Folder: FolderOutlined,
  FolderClosed: Folder,
  FolderOpen: FolderOpen,

  // Document Types
  Article: MenuBook,
  ArticleOutlined: ArticleOutlined,
  Form: Description,
  Print: PrintOutlined,
  Image: Image,
  Pdf: PictureAsPdf,

  // Settings & Configuration
  Settings: Settings,
  SettingsOutlined: SettingsOutlined,
  Checkmark: TaskAlt,


  // Actions
  Add: Add,
  New: Add,
  Edit: Edit,
  Copy: ContentCopy,
  Rename: DriveFileRenameOutline,
  Delete: DeleteForever,
  Save: Save,
  Undo: Undo,

  // Security & Access
  Locked: Lock,
  Unlocked: LockOpen,

  // Configuration Modes
  DevMode: Construction,
  Assignment: Assignment,
  Disabled: Block,
  Anonymous: VisibilityOff,
  InHouse: HomeWorkOutlined,

  // Utility
  Phone: Phone,
  Search: Search,
  Close: Close,
  Link: Link,
  Language: Language,
  Flow: AccountTree,
  Error: Error,
  Warning: Warning,
  ErrorOutline: ErrorOutline,
  Debug: BugReport,
  Preview: Visibility,
  Info: Info,
  Tree: AccountTree,
  History: WorkHistory,
  Help: Help,
  ArticleOrder: FormatListNumbered,
  Page: Feed,
  ChildItem: SubdirectoryArrowRight,
  DecisionTable: TableChart,

  // Decision Table Toolbar
  AddHeaderIn: DoubleArrowRounded,
  AddHeaderOut: DoubleArrowRounded,
  AddRow: DoubleArrowRounded,
  Organize: CompareArrowsRounded,
  CsvDownload: FileDownloadDone,
  CsvUpload: Upload,
  ArticleLocaleOverview: Check,
  Stats: Assessment,

  // State indicators
  Unsaved: FiberManualRecord,

  Label: LabelIcon,
  FlowTask: CodeOutlined,
  Description: NotesOutlined,
};

export interface FsIconProps {
  icon: React.ElementType<SvgIconProps>;
  xsmall?: boolean;
  small?: boolean;
  medium?: boolean;
  large?: boolean;
  className?: string;
  tooltip?: string;
  color?: string;
}

export const FsIcon: React.FC<FsIconProps> = ({ icon, xsmall, small, large, className, tooltip, color }) => {
  const Icon = icon;
  const ICON_SIZES = { xsmall: 8, small: 15, medium: 20, large: 24 };

  let fontSize = ICON_SIZES.medium;

  if (xsmall) {
    fontSize = ICON_SIZES.xsmall;
  }
  if (small) {
    fontSize = ICON_SIZES.small
  }
  if (large) {
    fontSize = ICON_SIZES.large
  }
  if (tooltip) {
    return (
      <Tooltip title={tooltip} arrow>
        <Icon className={className} sx={{ fontSize, color }} />
      </Tooltip>)
  }

  return (
    <Icon className={className} sx={{ fontSize, color }} />
  );
};
