import { Profile, Project } from 'client';
import { ProjectDescriptor, PaletteType, ProjectPaletteType, DescriptorState } from 'descriptor-project';


export interface ProjectsContextType {
  setState: ProjectsDispatch;
  reload: () => Promise<void>;
  loading: boolean;
  state: ProjectsState,
  palette: PaletteType;


}

export type ProjectsMutator = (prev: ProjectsState) => ProjectsState;
export type ProjectsDispatch = (mutator: ProjectsMutator) => void;

export interface ProjectsState {
  projects: ProjectDescriptor[];
  projectsByUser: Record<string, ProjectDescriptor[]>;
  palette: ProjectPaletteType;
  profile: Profile;
  users: string[];

  withProfile(profile: Profile): ProjectsState;
  withProjects(projects: Project[]): ProjectsState;
  withDescriptors(): DescriptorState;
}