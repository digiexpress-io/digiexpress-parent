
interface SecondaryContextType {
  session: SecondarySession;
  actions: SecondaryActions;
}

interface SecondarySession {
  appId: string;
  secondary?: string;
  withSecondary(newItemId?: string): SecondarySession;
}


interface SecondaryActions {
  handleSecondary(newItemId?: string): void;
}

export type { SecondaryContextType, SecondarySession, SecondaryActions };