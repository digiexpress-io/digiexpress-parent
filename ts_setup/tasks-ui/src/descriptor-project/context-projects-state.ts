
import { Project, UserProfileAndOrg } from 'client';

import { ProjectsState } from './types';
import { Palette, _nobody_ } from './constants';
import { ProjectDescriptor, ProjectPaletteType } from './types';
import { GroupsAndFiltersImpl, ProjectDescriptorImpl } from './types-impl';
import { withColors } from 'components-colors';


interface ExtendedInit extends Omit<ProjectsState, "withProfile" | "withProjects" | "toGroupsAndFilters"> {
  users: string[];
  palette: {
    users: Record<string, string>;
    repoType: Record<string, string>;
  }
  profile: UserProfileAndOrg;
}

class ProjectsStateBuilder implements ProjectsState {
  private _projects: ProjectDescriptor[];
  private _projectsByUser: Record<string, ProjectDescriptor[]>;
  private _users: string[];
  private _palette: ProjectPaletteType;
  private _profile: UserProfileAndOrg;

  constructor(init: ExtendedInit) {
    this._projects = init.projects;
    this._projectsByUser = init.projectsByUser;
    this._users = init.users;
    this._palette = init.palette;
    this._profile = init.profile;
  }
  get profile(): UserProfileAndOrg { return this._profile }
  get palette(): ProjectPaletteType { return this._palette }
  get users(): string[] { return this._users }
  get projects(): ProjectDescriptor[] { return this._projects }
  get projectsByUser(): Record<string, ProjectDescriptor[]> { return this._projectsByUser }

  toGroupsAndFilters(): GroupsAndFiltersImpl {
    return new GroupsAndFiltersImpl({
      data: this,
      filtered: this._projects,
      filterBy: [],
      groupBy: 'none',
      groups: [],
      searchString: undefined,
    });
  }
  withProfile(profile: UserProfileAndOrg): ProjectsState {
    return new ProjectsStateBuilder({ ...this.clone(), profile });
  }
  withProjects(input: Project[]): ProjectsState {
    const projects: ProjectDescriptor[] = [];

    const users: string[] = [_nobody_];
    const projectsByUser: Record<string, ProjectDescriptor[]> = {};
    const today = new Date(this._profile.today);
    today.setHours(0, 0, 0, 0);

    input.forEach(tenant => {
      const item = new ProjectDescriptorImpl(tenant, this._profile, today);
      projects.push(item);
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
