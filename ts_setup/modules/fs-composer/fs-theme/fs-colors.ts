
// =============================================================================
// ULTRA-CONSOLIDATED COLOR PALETTE (12 Unique Colors)
// =============================================================================

export const FsColors = {
  // Base theme colors
  dark: {
    background: '#1e1e1e',     // Main dark background
    surface: '#2d2d30',        // Cards, title bars, elevated surfaces
    border: '#3c3c3c',         // Dividers, borders, hover states
    text: '#cccccc',           // Primary text and icons
    textSecondary: '#888888',  // Meta info, placeholders
    textMuted: '#6a9955',      // Description text
  },

  light: {
    background: '#ffffff',     // Main light background
    surface: '#f5f5f5',        // Cards, title bars, elevated surfaces (also used for hover)
    border: '#e0e0e0',         // Dividers, borders
    text: '#333333',           // Primary text and icons
    textSecondary: '#666666',  // Secondary text, icons (also used for borderHover)
    textMuted: '#6a9955',      // Description text (shared with dark theme)
  },

  // Semantic accent colors
  semantic: {
    dangerLight: '#ae0e0e',    // Dark red for light backgrounds (9.3:1 contrast)
    dangerDark: '#ff8c8c',     // Bright red for dark backgrounds (7.1:1 contrast)
    warning: '#ffa500',        // Lock states, warnings (dark theme)
    warningLight: '#ff8c00',   // Lock states, warnings (light theme) - changed from red to orange
    success: '#228b22',        // Success states, links (also used for link icons in light theme)
    info: '#9cdcfe',           // Information, dialob forms (also used for form icons in dark theme)
    primary: '#4ec9b0',        // Primary actions, services (also used for service icons in dark theme)
    active: '#4c4b4b',         // Active menu item background
    highlightLight: '#eaea25', // Search result text highlighting
    highlightDark: '#403d3d'   // Search result text highlighting
  },

  // Node type colors - ultra-consolidated
  nodeTypes: {
    dark: {
      folder: '#e8e5e5',       // Neutral gray for containers
      content: '#dcdcaa',      // Yellow for content (articles)
      service: '#4ec9b0',      // Teal for services/systems (same as semantic.primary)
      form: '#9cdcfe',         // Light blue for forms/dialobs (same as semantic.info)
      flow: '#c586c0',         // Purple for workflows
      link: '#98d982',         // Green for external links
      document: '#ce9178',     // Orange for documents (language, template, printout)
      asset: '#dda0dd',        // Light purple for assets/images (changed from red)
    },
    light: {
      folder: '#333333',       // Dark gray for containers (same as text)
      content: '#8b008b',      // Dark magenta for content (articles and flows merged)
      service: '#1f5f3f',      // Dark green for services/systems
      form: '#0056b3',         // Dark blue for forms/dialobs
      flow: '#8b008b',         // Dark magenta for workflows (merged with content)
      link: '#228b22',         // Forest green for external links (same as semantic.success)
      document: '#5d2f0a',     // Darker brown for all documents (language, template, printout) - made darker for better visibility
      asset: '#663399',        // Dark purple for assets/images - changed from red
    }
  }
} as const;

// =============================================================================
// HELPER FUNCTIONS
// =============================================================================

export type FsNodeType = 'folder' | 'article' | 'service' | 'dialob' | 'flow' | 'link' | 'language' | 'printout' | 'image' | 'template';

export function getNodeColor(nodeType: FsNodeType, isDarkTheme: boolean) {
  const colors = isDarkTheme ? FsColors.nodeTypes.dark : FsColors.nodeTypes.light;

  switch (nodeType) {
    case 'folder':
      return colors.folder;
    case 'article':
      return colors.content;
    case 'service':
      return colors.service;
    case 'dialob':
      return colors.form;
    case 'flow':
      return colors.flow;
    case 'link':
      return colors.link;
    case 'language':
    case 'template':
    case 'printout':
      return colors.document;
    case 'image':
      return colors.asset;
    default:
      return isDarkTheme ? FsColors.dark.text : FsColors.light.text;
  }
}

export function getThemeColors(isDarkTheme: boolean) {
  return isDarkTheme ? FsColors.dark : FsColors.light;
}