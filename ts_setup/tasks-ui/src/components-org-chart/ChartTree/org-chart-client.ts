import { ChartProps, ChartNode, BorderShape } from './org-chart-types';


const ONE_NODE_COLSPAN = 2;

function calcColSpan(props: ChartProps): {
  colspan: number;
  left: number;
  right: number;
  node: number;
} {
  const totalChildren: number = props.children?.length ?? 0;
  const totalCols = Math.max(totalChildren * ONE_NODE_COLSPAN, ONE_NODE_COLSPAN);
  const leftover = totalCols - ONE_NODE_COLSPAN;

  if (leftover === 0) {
    return {
      colspan: totalCols,
      left: 0,
      right: 0,
      node: ONE_NODE_COLSPAN
    };
  }
  const equallyDistributedColsOnBothSides = leftover / 2;
  return {
    colspan: totalCols,
    left: equallyDistributedColsOnBothSides,
    right: equallyDistributedColsOnBothSides,
    node: ONE_NODE_COLSPAN
  };
}

function calcChildren(colspan: number): { shapes: BorderShape[] }[] {
  const row3Cells: { shapes: BorderShape[] }[] = [];

  for (let index = 0; index < colspan; index++) {
    if (colspan < 3) {
      row3Cells.push({ shapes: [] })
      continue;
    }
    const isEven = index % 2 == 0;
    const borderRight: BorderShape[] = isEven ? ['BORDER_RIGHT'] : [];
    const borderTop: BorderShape[] = index === 0 || index === colspan - 1 ? [] : ['BORDER_TOP'];

    row3Cells.push({
      shapes: [
        ...borderRight,
        ...borderTop,
      ]
    })
  }
  return row3Cells;
}

export function createChartNode(value: ChartProps): ChartNode {
  const colSpans = calcColSpan(value);
  const children = calcChildren(colSpans.colspan);
  return { id: value.id, children, ...colSpans };
}