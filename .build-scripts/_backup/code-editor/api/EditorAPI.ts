export interface ViewCommand {
  id: number;
  type: 'SET' | 'DELETE' | 'ADD';
  value: string;
}

export type ViewMode = "ft" | "fl" | "json";
export type ViewLang = "yaml" | "groovy" | "json";

export interface ViewProps {
  mode: ViewLang;
  src: string;
  theme: string;
}

export interface ViewEvents {
  onChanges?: (commands: ViewCommand[], content: string) => void;  
  lint?: () => LintMessage[];
  hint?: (pos: CodeMirror.Position, content: string) => CodeMirror.Hints;
}

export interface LintMessage { line: number; value: string; type: "ERROR" | "WARNING"; range?: LintRange; }
export interface LintRange { start: number; end: number; column?: number; insert?: boolean; }

export interface View {
  withValue(value: string): View;
  withEvents(events: ViewEvents): View
}