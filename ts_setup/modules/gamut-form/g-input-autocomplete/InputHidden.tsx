import React from "react";



export const InputHiddenMulti: React.FC<{ 
  id: string;
  value: string[] | null;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}> = (props) => {
  const {id, value, onChange} = props;

  
  const newInputValue = value ?? [] ;
  const [inputValue, setInputValue] = React.useState<string[] | null>(value)
  const ref = React.useRef<HTMLInputElement>(null); 

  React.useEffect(() => {
    if(inputValue !== newInputValue) {
      setInputValue(newInputValue);
      const event = new Event('input', { bubbles: true });
      ref.current?.dispatchEvent(event);
    }
  }, [inputValue, value]);

  // trigger event on the hidden input that will contain technical date
  React.useEffect(() => {
    function poulateTheChange(event: any) {
      onChange(event);
    }
    ref.current?.addEventListener("input", poulateTheChange);
    return () => ref.current?.removeEventListener("input", poulateTheChange);
  }, [onChange]);

  return (<input name={id} hidden value={newInputValue} ref={ref} onChange={() => {}} />);
}


export const InputHiddenUni: React.FC<{ 
  id: string;
  value: string | null;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}> = (props) => {

  const {id, value, onChange} = props;  
  const newInputValue = value ?? '';
  const [inputValue, setInputValue] = React.useState<string | null>(value)
  const ref = React.useRef<HTMLInputElement>(null); 

  React.useEffect(() => {
    if(inputValue !== newInputValue) {
      setInputValue(newInputValue);
      const event = new Event('input', { bubbles: true });
      ref.current?.dispatchEvent(event);
    }
  }, [inputValue, value]);

  // trigger event on the hidden input that will contain technical date
  React.useEffect(() => {
    function poulateTheChange(event: any) {
      onChange(event);
    }
    ref.current?.addEventListener("input", poulateTheChange);
    return () => ref.current?.removeEventListener("input", poulateTheChange);
  }, [onChange]);

  return (<input name={id} hidden value={newInputValue} ref={ref} onChange={() => {}} />);
}