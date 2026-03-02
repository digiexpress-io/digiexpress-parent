import { FsNode } from "@dxs-ts/fs-api";


export interface FsHistoryProps {
  node: FsNode | undefined;
}


interface ItemHistoryEntry {
  user: string;
  change: string;
  date: string;
}

export const historyData: ItemHistoryEntry[] = [
  { user: 'Diana Hasselback', change: 'Updated content', date: '15.01.2025' },
  { user: 'office-staff', change: 'Modified labels', date: '14.01.2025' },
  { user: 'John Smith', change: 'Updated description', date: '13.01.2025' },
  { user: 'Diana Hasselback', change: 'Configuration changed', date: '12.01.2025' },
  { user: 'part-time staff', change: 'Content review', date: '10.01.2025' },
  { user: 'John Smith', change: 'Updated permissions', date: '08.01.2025' },
  { user: 'office-staff', change: 'Added labels', date: '05.01.2025' },
  { user: 'Diana Hasselback', change: 'Content updated', date: '03.01.2025' },
  { user: 'John Smith', change: 'Structure modified', date: '28.12.2024' },
  { user: 'System', change: 'File created', date: '20.12.2024' }
];