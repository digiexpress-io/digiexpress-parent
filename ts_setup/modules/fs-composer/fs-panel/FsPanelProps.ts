

export interface FsPanelProps {
  title: string;
  icon?: React.ReactNode;
  children: React.ReactNode;
  secondaryChildren?: React.ReactNode;
  noNodeMessage?: string;
  activeNode?: boolean;
}