import { Palette } from './descriptor-constants';

export * from './descriptor-types';

export { ProjectsContext, ProjectsProvider } from './projects-ctx';
export { ProjectIdProvider, ProjectIdContext } from './project-id-ctx';

export type { ProjectIdContextType } from './project-id-ctx';
export type { ProjectsContextType } from './projects-ctx-types';

export const RepoTypePalette = Palette.repoType;
export { ProjectDescriptorImpl, DescriptorStateImpl } from './descriptor-impl';