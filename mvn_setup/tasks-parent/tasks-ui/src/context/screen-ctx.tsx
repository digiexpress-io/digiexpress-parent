import React from 'react';
import { ScreenContextType, ScreenMutator, ScreenDispatch, ScreenMutatorBuilder } from './screen-ctx-types';
import { ScreenStateBuilder } from './screen-ctx-impl';


const ScreenContext = React.createContext<ScreenContextType>({} as ScreenContextType);


const ScreenProvider: React.FC<{ children: React.ReactNode }> = ({ children}) => {

  const [state, setState] = React.useState<ScreenMutatorBuilder>(new ScreenStateBuilder({ width: 0, height: 0, load: true }));
  const setter: ScreenDispatch = React.useCallback((mutator: ScreenMutator) => setState(mutator), [setState]);

  const ref = React.createRef<HTMLDivElement>();
  const [lastEventTime, setEvents] = React.useState<number>(0);
  const [check, setCheck] = React.useState<number>(0);

  const updateScreen = React.useCallback(() => {
    setEvents(Date.now());
  }, [setEvents]);


  React.useEffect(() => {
    if (lastEventTime === 0) {
      return;
    }
    const delay = 500;
    const diff = Date.now() - lastEventTime;
    if (diff > delay) {
      setEvents(0);
      setCheck(0);
      setState(prev => prev.withLoad(true));
    } else {
      setTimeout(() => {
        setCheck(Date.now())
      }, delay);
    }

  }, [lastEventTime, setEvents, setCheck, check, setState]);


  React.useEffect(() => {
    const current = ref.current;
    if (!current) {
      return;
    }
    window.addEventListener("resize", updateScreen, false);
    return function cleanup() {
      window.removeEventListener("resize", updateScreen, false);
    }
  }, [ref, updateScreen]);


  React.useEffect(() => {
    if (!ref.current || !state.load) {
      return;
    }

    const current = ref.current;
    const height = current?.offsetWidth;
    const width = current?.offsetHeight;
    const value = { height, width };
    
    console.log("SCREEN:: recalculating screen", value);
    setState(prev => prev.withScreen(value))

  }, [state, ref]);




  const contextValue: ScreenContextType = React.useMemo(() => {
    return { state, setState: setter, loading: false };
  }, [state, setter]);

  return (<ScreenContext.Provider value={contextValue}>

    <div style={{ width: '100vw', height: '100vh' }} ref={ref}>{children}</div>
  </ScreenContext.Provider>);
};


export { ScreenProvider, ScreenContext };

