interface MenuContextType {
  activeTab: MenuTab;
  withTab: (tab: MenuTab) => void;
  resetTab: () => void;
}

type MenuTab = 'attachments' | 'checklists' | 'messages';

export type { MenuContextType, MenuTab }
