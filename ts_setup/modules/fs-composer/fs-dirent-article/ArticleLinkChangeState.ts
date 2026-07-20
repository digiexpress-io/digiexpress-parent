export class ArticleLinkChangeState {
  private readonly _origin: readonly string[];
  private readonly _previous: readonly string[];
  private readonly _current: readonly string[];

  constructor(origin: string[], previous?: string[], current?: string[]) {
    this._origin = origin;
    this._previous = previous ?? [...origin];
    this._current = current ?? [...origin];
  }

  get pendingLinks(): string[] {
    return [...this._current];
  }
  get addedFromPrevious(): string[] {
    return this._current.filter(id => !this._previous.includes(id));
  }
  get removedFromPrevious(): string[] {
    return this._previous.filter(id => !this._current.includes(id));
  }

  withLinks(links: string[]): ArticleLinkChangeState {
    return new ArticleLinkChangeState([...this._origin], [...this._current], links);
  }
}
