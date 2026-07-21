import React from 'react';
import { Fs, useFsDirent, FsuChange, useFsuChange, useFsu } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { createWidget } from '../fs-factory';
import { createLinkFsuChange, LinkArticleChange } from '../fs-dirent-article-link/useUpdateOwnerState';
import { ArticleLinkChangeState } from './ArticleLinkChangeState';

export interface UpdateOwnerState {
  assetPath: string | undefined;
  dirent: Fs.DirentBase | undefined;
  id: string;
  isDirty: boolean;
  name: string;
  orderNumber: string;
  configOptions: Fs.ConfigOption[];
  links: string[];
  onChangeName: (value: string) => void;
  onChangeOrderNumber: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeLinks: (value: string[]) => void;
}

type _ChangeStateProps = {
  articleId: string;
  bodyType: Fs.BodyType;
  name: string;
  order: number;
  configOptions: Fs.ConfigOption[];
  treeId: string;
  linkState: ArticleLinkChangeState;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() {
    return this._current.articleId;
  }
  get name() {
    return this._current.name;
  }
  get treeId() {
    return this._current.treeId;
  }
  get orderNumber() {
    return String(this._current.order);
  }
  get configOptions() {
    return this._current.configOptions;
  }
  get bodyType() {
    return this._current.bodyType;
  }
  get links() {
    return this._current.linkState.pendingLinks;
  }
  get linkState() {
    return this._current.linkState;
  }
  get isDirty(): boolean {
    const { linkState: _ol, ...originRest } = this._origin;
    const { linkState: _cl, ...currentRest } = this._current;
    return JSON.stringify(originRest) !== JSON.stringify(currentRest);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: this.id,
      changes: {
        articleId: c.articleId,
        name: c.name,
        order: c.order,
        devMode: c.configOptions.includes('DEV_MODE') || undefined,
        authOnly: c.configOptions.includes('AUTH_ONLY_MODE') || undefined,
      }
    };
  }

  withName(name: string): _ChangeState {
    return new _ChangeState({ ...this._current, name }, this._origin);
  }
  withOrder(order: string): _ChangeState {
    return new _ChangeState({ ...this._current, order: parseInt(order) || 0 }, this._origin);
  }
  withConfigOptions(value: string[]): _ChangeState {
    const widget = createWidget({ type: 'ARTICLE' });
    return new _ChangeState({
      ...this._current,
      configOptions: widget.meta.configOptions.filter(opt => value.includes(opt)),
    }, this._origin);
  }
  withLinks(pendingLinks: string[]): _ChangeState {
    return new _ChangeState({
      ...this._current,
      linkState: this._current.linkState.withLinks(pendingLinks),
    }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { activeTabPath } = useFsNav();
  const { getDirent, getDirentName, selectOptions } = useFsDirent();
  const fsu = useFsu();

  const dirent = getDirent(props.direntId);
  const articleProps = dirent?.type === 'ARTICLE' ? dirent.props as Fs.ArticleProps : undefined;

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    articleId: props.direntId,
    bodyType: dirent!.type,
    treeId: dirent?.commitIndex?.treeId!,
    name: getDirentName(props.direntId) ?? '',
    order: articleProps?.orderNumber ?? 0,
    configOptions: (articleProps?.configOptions ?? []) as Fs.ConfigOption[],
    linkState: new ArticleLinkChangeState(
      selectOptions.links
        .filter(l => ((getDirent(l.value)?.props as Fs.LinkProps | undefined)?.articles ?? []).includes(props.direntId))
        .map(l => l.value)
    ),
  }));

  function applyLinkArticleChange(linkId: string, transform: (articles: string[]) => string[]) {
    const linkDirent = getDirent(linkId);
    if (!linkDirent) {
      return;
    }
    const currentArticles = fsu.isChange(linkId) ? (fsu.getChange(linkId) as LinkArticleChange).articles : ((linkDirent.props as Fs.LinkProps)?.articles ?? []);
    const newArticles = transform(currentArticles);
    if (fsu.isChange(linkId)) {
      fsu.withChange<LinkArticleChange>(linkId, prevChange => prevChange.withArticles(newArticles));
    } else {
      fsu.withNewChange(linkId, () => createLinkFsuChange(linkDirent, newArticles));
    }
  }

  React.useEffect(() => {
    for (const linkId of state.linkState.addedFromPrevious) {
      applyLinkArticleChange(linkId, articles => [...articles.filter(id => id !== props.direntId), props.direntId]);
    }
    for (const linkId of state.linkState.removedFromPrevious) {
      applyLinkArticleChange(linkId, articles => articles.filter(id => id !== props.direntId));
    }
  }, [state.linkState]);

  function onChangeName(value: string) {
    update(prev => prev.withName(value));
  }
  function onChangeOrderNumber(value: string) {
    update(prev => prev.withOrder(value));
  }
  function onChangeConfigOptions(value: string[]) {
    update(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }
  function onChangeLinks(newLinkIds: string[]) {
    update(prev => prev.withLinks(newLinkIds));
  }

  return ({
    assetPath: activeTabPath,
    dirent,
    id: state.id,
    isDirty: state.isDirty,
    name: state.name,
    orderNumber: state.orderNumber,
    configOptions: state.configOptions,
    links: state.links,
    onChangeName,
    onChangeOrderNumber,
    onChangeConfigOptions,
    onChangeLinks,
  });
};
