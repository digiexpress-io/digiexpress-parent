import { } from "@mui/styles";
import { components } from "./components";
import { palette } from "./palette";
import { typography } from "./typography";

declare module '@mui/material/Button' {
  interface ButtonPropsVariantOverrides {
    explorerInactive: true;
    explorerActive: true;
  }
}

declare module 'react' {
  interface CSSProperties {
    '--tree-view-text-color'?: string;
    '--tree-view-color'?: string;
    '--tree-view-bg-color'?: string;
    '--tree-view-hover-color'?: string;
  }
}


export const eveliTheme = {
  components, palette, typography
}