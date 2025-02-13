
interface DrawerContextType {
  session: DrawerSession;
  actions: DrawerActions;
}

interface DrawerSession {
  drawer: boolean;
  withDrawer(open: boolean): DrawerSession;
}

interface DrawerActions {
  handleDrawerOpen(open: boolean): void;
}

export type { DrawerContextType, DrawerSession, DrawerActions };