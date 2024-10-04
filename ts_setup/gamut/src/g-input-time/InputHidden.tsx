import React from 'react';



export const InputHidden: React.FC<{ 
  id: string;
  time: string | null;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}> = (props) => {
  const {id, time, onChange} = props;
  const newInputValue = time ?? '';
  const [inputValue, setInputValue] = React.useState(time)
  const ref = React.useRef<HTMLInputElement>(null); 


  React.useEffect(() => {

    if(inputValue !== newInputValue) {
      setInputValue(newInputValue);
      const event = new Event('input', { bubbles: true });
      ref.current?.dispatchEvent(event);
    }
  }, [inputValue, time]);

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