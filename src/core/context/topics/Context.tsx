import React from 'react';
import { Topic, TopicLink } from '../../service';
import { contextReducer, TopicsState, ImmutableTopicsState, TopicsReducerDispatch } from './contextReducer';
import { TopicsContextType, initContext, TopicsActionOverrides } from './ContextTypes';

interface TopicsProviderProps {
  children: React.ReactNode; 
  overrides?: TopicsActionOverrides;
}

const initState = (): TopicsState => {
  console.log("portal: Topics state init");
  return new ImmutableTopicsState({});
};

const TopicsProvider: React.FC<TopicsProviderProps> = (props) => {
  const { overrides } = props;
  const init = React.useMemo(() => initState(), [])
  const [state, dispatch] = React.useReducer(contextReducer, init);
  const actions = React.useMemo(() => new TopicsReducerDispatch(dispatch, overrides ? overrides : {}), [dispatch, overrides]);
  const contextValue = React.useMemo(() => initContext(state, actions), [state, actions]);

  return (<TopicsContext.Provider value={contextValue}>{props.children}</TopicsContext.Provider>);
}

const TopicsContext = React.createContext<TopicsContextType>({
  actions: {} as any,
  setTopic: (newTopic: Topic) => console.log(newTopic),
  setLink: (newLink?: TopicLink) => console.log(newLink),
})

export type { TopicsProviderProps };
export { TopicsContext, TopicsProvider };


