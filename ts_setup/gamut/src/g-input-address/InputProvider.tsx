import React from 'react'



export interface InputContextType {
  setValue: (newValue: string | undefined | null) => void
}

export const InputContext = React.createContext<InputContextType>({} as any);

export interface InputProviderProps {
  id: string;
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  children: React.ReactNode;
}


export const InputProvider: React.FC<InputProviderProps> = (props) => {
  const {id, value: backendState, onChange} = props;

  const [inputValue, setInputValue] = React.useState<string | undefined | null>(backendState)
  const ref = React.useRef<HTMLInputElement>(null); 


  React.useEffect(() => {

    if(inputValue !== backendState) {

      console.log("triggering");
      const event = new Event('input', { bubbles: true });
      ref.current?.dispatchEvent(event);
    }
  }, [inputValue, backendState]);

  // trigger event on the hidden input that will contain technical date
  React.useEffect(() => {
    function poulateTheChange(event: any) {
      onChange(event);
    }
    ref.current?.addEventListener("input", poulateTheChange);
    return () => ref.current?.removeEventListener("input", poulateTheChange);
  }, [onChange]);


  
  const contextValue: InputContextType = React.useMemo(() => Object.freeze({ setValue: setInputValue }), [setInputValue]);
  return (<InputContext.Provider value={contextValue}>
    <input name={id} hidden value={inputValue ?? ''} ref={ref} onChange={() => {}} />
    {props.children}
  </InputContext.Provider>);
}


export const useInput = () => {
  return React.useContext(InputContext);
}





