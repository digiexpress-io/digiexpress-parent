
// =============================================================================
// ULTRA-CONSOLIDATED COLOR PALETTE (12 Unique Colors)
// =============================================================================

export const FsColors = {
  // Base theme colors
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
    warning: '#ffa500',        // Lock states, warnings (dark theme)
    warningLight: '#ff8c00',   // Lock states, warnings (light theme) - changed from red to orange
    success: '#228b22',        // Success states, links (also used for link icons in light theme)
    info: '#9cdcfe',           // Information, dialob forms (also used for form icons in dark theme)
    primary: '#4ec9b0',        // Primary actions, services (also used for service icons in dark theme)
    secondary: '#b1f8ea',        // Primary actions, services (also used for service icons in dark theme)
    active: '#4c4b4b',         // Active menu item background
    highlightLight: '#eaea25', // Search result text highlighting
  },

  // Dirent type colors - ultra-consolidated
  direntTypes: {
    light: {
      folder: '#333333',       // Dark gray for containers (same as text)
      article: '#8b008b',      // Dark magenta for content (articles and flows merged)
      service: '#1f5f3f',      // Dark green for services/systems
      form: '#0056b3',         // Dark blue for forms/dialobs
      flow: '#8b008b',         // Dark magenta for workflows (merged with content)
      link: '#228b22',         // Forest green for external links (same as semantic.success)
      document: '#5d2f0a',     // Brown for documents (template)
      language: '#455a64',     // Dark blue-grey for global language definitions
      printout: '#5d4037',     // Brown for printout documents
      asset: '#663399',        // Dark purple for assets/images - changed from red
      phone: '#880e4f',        // Dark rose for phone numbers
      page: '#00838f',         // Dark cyan for pages (localised markdown content)
    }
  }
} as const;

// =============================================================================
// HELPER FUNCTIONS
// =============================================================================



export function getThemeColors() {
  return FsColors.light;
}