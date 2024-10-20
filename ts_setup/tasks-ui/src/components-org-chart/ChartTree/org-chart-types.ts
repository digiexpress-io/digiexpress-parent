export interface ChartProps {
  id: string;
  label: React.ReactNode;
  expanded?: boolean;
  children?: ChartProps[];
}

export type BorderShape = 'BORDER_RIGHT' | 'BORDER_TOP';

export interface ChartNode {
  id: string;

  colspan: number;
  left: number;
  right: number;
  node: number;

  children: { shapes: BorderShape[] }[]; // child cells
}