
export interface IconbarContextType {
  activeId: string | undefined;

  handleActiveId(newActiveId: string | undefined): void;
}
