import { Handle, useStore, Position } from '@xyflow/react';


export function useNode(id: string) {
  const target = useStore((s) => {
    const node = s.nodeLookup.get(id);
    if (!node) {
      return null;
    }
    return node;
  });

  return target;
}