import { ModuleInfo, DependencyGraph } from '../module-registry-types';

export declare namespace Command_CreateDepGraph {
  export interface Input {
    moduleInfos: ModuleInfo[];
  }

  export interface Result {
    dependencyGraph: DependencyGraph;
  }
}

export class Command_CreateDepGraph {
  execute(input: Command_CreateDepGraph.Input): Command_CreateDepGraph.Result {
    const { moduleInfos } = input;

    const forward: Record<string, string[]> = {};
    const reverse: Record<string, string[]> = {};

    // Initialize empty arrays for all modules
    for (const moduleInfo of moduleInfos) {
      forward[moduleInfo.name] = [];
      reverse[moduleInfo.name] = [];
    }

    // Build forward dependencies
    for (const moduleInfo of moduleInfos) {
      const internalDeps = moduleInfo.internalDependencies;
      forward[moduleInfo.name] = internalDeps;

      // Build reverse dependencies
      for (const dep of internalDeps) {
        if (!reverse[dep]) {
          reverse[dep] = [];
        }
        reverse[dep].push(moduleInfo.name);
      }
    }

    // Detect circular dependencies
    const circularDependencies = _detectCircularDependencies(forward);

    if (circularDependencies.length > 0) {
      console.warn(`❌ Found ${circularDependencies.length} circular dependencies:`);
      circularDependencies.forEach(cycle => {
        console.warn(`   ${cycle.join(' → ')}`);
      });
    }

    const dependencyGraph: DependencyGraph = {
      forward,
      reverse,
      circularDependencies
    };

    return { dependencyGraph };
  }
}

// Pure transformative functions
function _detectCircularDependencies(forward: Record<string, string[]>): string[][] {
  const cycles: string[][] = [];
  const visited = new Set<string>();
  const recursionStack = new Set<string>();

  const dfs = (module: string, path: string[]): void => {
    if (recursionStack.has(module)) {
      // Found a cycle - extract the cycle from the path
      const cycleStart = path.indexOf(module);
      const cycle = path.slice(cycleStart).concat(module);
      cycles.push(cycle);
      return;
    }

    if (visited.has(module)) return;

    visited.add(module);
    recursionStack.add(module);
    path.push(module);

    const dependencies = forward[module] || [];
    for (const dep of dependencies) {
      dfs(dep, [...path]);
    }

    recursionStack.delete(module);
    path.pop();
  };

  // Check each module for cycles
  for (const module of Object.keys(forward)) {
    if (!visited.has(module)) {
      dfs(module, []);
    }
  }

  return cycles;
}