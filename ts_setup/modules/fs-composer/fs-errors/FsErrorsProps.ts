import { FsNode } from "@dxs-ts/fs-api";


export interface FsErrorsProps {
  node: FsNode | undefined;
}

export const errorsMock = [
  {
    id: 1,
    severity: 'WARNING',
    title: 'Missing Translation Warning',
    description: 'Main.article does not have a Finnish language page',
    affectedFile: 'fi.language',
    timestamp: '12.02.2025 14:30'
  },
  {
    id: 2,
    severity: 'ERROR',
    title: 'Missing Markdown level 1 heading error',
    description: 'Page in main.article cannot be rendered in portal if no level 1 heading is defined.',
    affectedFile: 'main.article',
    timestamp: '12.02.2025 14:28'
  },
  {
    id: 3,
    severity: 'WARNING',
    title: 'Deprecated Service Reference',
    description: 'This node references "old-message.service" which has been marked as deprecated.',
    affectedFile: 'main.article',
    timestamp: '10.02.2025 09:15'
  },
  {
    id: 4,
    severity: 'ERROR',
    title: 'Broken Reference Link',
    description: 'The reference to "ref.article" could not be resolved in the tree structure.',
    affectedFile: 'main.article',
    timestamp: '11.02.2025 16:45'
  }
];
