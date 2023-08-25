interface MenuContextType {
  activeTab: MenuTab;
  withTab: (tab: MenuTab) => void;
}

type MenuTab = 'attachments' | 'checklists' | 'messages';

export type { MenuContextType, MenuTab }
