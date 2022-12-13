
export interface HdesTree {
  flows: Record<string, HdesAstFlow>;
  services: Map<string, HdesAstService>;
  decisions: Map<string, HdesAstDecision>;
}

export interface HdesAstFlow {
  id: string;
  ast: {
    name: string;
  }
}
export interface HdesAstService {
  id: string;
  ast: {
    name: string;
  }
}
export interface HdesAstDecision {
  id: string;
  ast: {
    name: string;
  }
}
