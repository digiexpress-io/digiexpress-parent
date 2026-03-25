import React from 'react';

import {
  // Navigation & Expansion Icons
  ExpandMore,
  ChevronRight,
  UnfoldLess,
  UnfoldMore,

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

  // Utility Icons
  Search,
  Close,
  Link,
  Language,
  Build,
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

  // Utility
  Phone: Phone,
  Search: Search,
  Close: Close,
  Link: Link,
  Language: Language,
  Flow: Build,
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
