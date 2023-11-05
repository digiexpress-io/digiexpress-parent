import React from 'react';
import { ProjectsContextType, ProjectsMutator, ProjectsDispatch, ProjectsState } from './projects-ctx-types';
import { ProjectsStateBuilder } from './projects-ctx-impl';
import { Backend, Profile } from 'client';
import { Palette } from 'descriptor-project';

const ProjectsContext = React.createContext<ProjectsContextType>({} as ProjectsContextType);


const startStart: ProjectsState = new ProjectsStateBuilder({
  users: [],
  projects: [],
  projectsByUser: {},
  palette: {
    users: {},
    repoType: {},
  },
  profile: { contentType: "OK", name: "", userId: "", today: new Date(), roles: [] }
});

const ProjectsProvider: React.FC<{ children: React.ReactNode, init: { backend: Backend, profile: Profile } }> = ({ children, init }) => {

  const { backend, profile } = init;

  const [loading, setLoading] = React.useState<boolean>(true);
  const [state, setState] = React.useState<ProjectsState>(startStart.withProfile(profile));
  const setter: ProjectsDispatch = React.useCallback((mutator: ProjectsMutator) => setState(mutator), [setState]);

  const contextValue: ProjectsContextType = React.useMemo(() => {
    return {
      state, setState: setter, loading, palette: Palette, reload: async () => {
        backend.project.getActiveProjects().then(data => {
          setState(prev => prev.withProjects(data.records))
        });
      }
    };
  }, [state, setter, loading, backend]);

  React.useEffect(() => {
    if (!loading) {
      return;
    }
    backend.project.getActiveProjects().then(data => {
      setLoading(false);
      setState(prev => prev.withProfile(profile).withProjects(data.records))
    });

  }, [loading, setLoading, backend, profile]);

  return (<ProjectsContext.Provider value={contextValue}>{children}</ProjectsContext.Provider>);
};


export { ProjectsProvider, ProjectsContext };

