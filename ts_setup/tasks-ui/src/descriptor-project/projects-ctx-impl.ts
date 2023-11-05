
import { ProjectsState } from './projects-ctx-types';

import { Project, Profile } from 'client';
import { Palette, _nobody_ } from './descriptor-constants';
import { DescriptorState, ProjectDescriptor, ProjectPaletteType } from './descriptor-types';
import { DescriptorStateImpl, ProjectDescriptorImpl } from './descriptor-impl';
import { withColors } from './descriptor-util';


interface ExtendedInit extends Omit<ProjectsState, "withProfile" | "withProjects" | "withDescriptors"> {
  users: string[];
  palette: {
    users: Record<string, string>
    repoType: Record<string, string>
  }
  profile: Profile
}

class ProjectsStateBuilder implements ProjectsState {
  private _projects: ProjectDescriptor[];
  private _projectsByUser: Record<string, ProjectDescriptor[]>;
  private _users: string[];
  private _palette: ProjectPaletteType;
  private _profile: Profile;

  constructor(init: ExtendedInit) {
    this._projects = init.projects;
    this._projectsByUser = init.projectsByUser;
    this._users = init.users;
    this._palette = init.palette;
    this._profile = init.profile;
  }
  get profile(): Profile { return this._profile }
  get palette(): ProjectPaletteType { return this._palette }
  get users(): string[] { return this._users }
  get projects(): ProjectDescriptor[] { return this._projects }
  get projectsByUser(): Record<string, ProjectDescriptor[]> { return this._projectsByUser }

  withDescriptors(): DescriptorState {
    return new DescriptorStateImpl({
      data: this,
      filtered: this._projects,
      filterBy: [],
      groupBy: 'none',
      groups: [],
      searchString: undefined,
    });
  }
  withProfile(profile: Profile): ProjectsStateBuilder {
    return new ProjectsStateBuilder({ ...this.clone(), profile });
  }
  withProjects(input: Project[]): ProjectsStateBuilder {
    const projects: ProjectDescriptor[] = [];

    const users: string[] = [_nobody_];
    const projectsByUser: Record<string, ProjectDescriptor[]> = {};
    const today = new Date(this._profile.today);
    today.setHours(0, 0, 0, 0);

    input.forEach(task => {
      const item = new ProjectDescriptorImpl(task, this._profile, today);
      projects.push(item);

      task.users.forEach(owner => {
        if (!users.includes(owner)) {
          users.push(owner)
        }

        if (!projectsByUser[owner]) {
          projectsByUser[owner] = [];
        }
        projectsByUser[owner].push(item);

      });
    });

    users.sort();

    const palette: ProjectPaletteType = {
      users: {},
      repoType: Palette.repoType
    }
    withColors(users).forEach(e => palette.users[e.value] = e.color);


    return new ProjectsStateBuilder({
      ...this.clone(),
      users,
      palette,
      projects,
      projectsByUser
    });
  }
  clone(): ExtendedInit {
    const init = this;
    return {
      profile: init.profile,
      projects: init.projects,
      projectsByUser: init.projectsByUser,
      users: init.users,
      palette: init.palette,
    }
  }
}

export { ProjectsStateBuilder };
