import React from 'react';



export const InputHidden: React.FC<{ 
  id: string;
  choice: string | null;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}> = (props) => {
  const {id, choice, onChange} = props;
  const newInputValue = choice ?? '';
  const [inputValue, setInputValue] = React.useState(choice)
  const ref = React.useRef<HTMLInputElement>(null); 


  React.useEffect(() => {

    if(inputValue !== newInputValue) {
      setInputValue(newInputValue);
      const event = new Event('input', { bubbles: true });
      ref.current?.dispatchEvent(event);
    }
  }, [inputValue, choice]);

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