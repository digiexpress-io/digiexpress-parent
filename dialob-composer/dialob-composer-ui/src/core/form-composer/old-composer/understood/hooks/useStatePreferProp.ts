import { Dispatch, SetStateAction, useState } from "react";

export function useStatePreferProp<S>(initialState: S | (() => S), prop: S | undefined | null): [S, Dispatch<SetStateAction<S>>] {
  const [state, setState] = useState(initialState);
  if(prop) return [prop, setState];
  return [state, setState];
}
