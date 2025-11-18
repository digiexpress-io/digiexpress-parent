import React from "react";
import { Store } from '@tanstack/react-store'
import { DialobApi } from "@dxs-ts/gamut-api";

interface RegisteredRefs {
  pageId: string;
  values: Record<string, React.MutableRefObject<any>>;
  md5: string;
  firstItemId: string;
  consumed: boolean;
}

class RefState {
  private store: Store<Record<string, RegisteredRefs>>;
  private pageId: string;

  constructor(pageId: string) {
    this.pageId = pageId;
    this.store = new Store<Record<string, RegisteredRefs>>({});
  }
  initPage(page: DialobApi.ControlPage) {
    this.store.setState((state) => {
      const prev = state[page.id];
      if (prev && page.errorChecksum === prev.md5) {
        return state;
      }
      if (prev && page.errorChecksum !== prev.md5) {
        const next = { ...state };
        next[page.id] = {
          ...prev,
          md5: page.errorChecksum ?? '',
          firstItemId: page.firstErrorControlId ?? '',
        }
        return next;
      }
  
      const next = { ...state };
      next[page.id] = {
        pageId: page.id,
        md5: page.errorChecksum ?? '',
        firstItemId: page.firstErrorControlId ?? '',
        values: {},
        consumed: true,
      }
      return next;
    })
  }

  consumed(props: { consumed: boolean }) {
    this.store.setState((state) => {
      const next = { ...state };
      const prev = next[this.pageId] ?? {};
      const page: RegisteredRefs = {
        firstItemId: prev.firstItemId ?? '',
        md5: prev.md5 ?? '',
        pageId: this.pageId,
        values: prev.values ?? {},
        consumed: props.consumed,
      };
      next[this.pageId] = page;
      return next
    })

    if(!props.consumed) {
      setTimeout(() => this.scrollTo(), 250);
    }
  }

  register(props: { id: string, ref: React.MutableRefObject<any> }) {
    this.store.setState((state) => {
      const next = { ...state };
      const prev = next[this.pageId] ?? {};
      const page: RegisteredRefs = {
        firstItemId: prev.firstItemId ?? '',
        md5: prev.md5 ?? '',
        pageId: this.pageId,
        values: prev.values ?? {},
        consumed: true,
      };
      page.values[props.id] = props.ref;
      next[this.pageId] = page;
      return next
    })
  }

  unregister(props: { id: string }) {
    this.store.setState((state) => {
      const next = { ...state };
      const page: RegisteredRefs = { ...next[this.pageId] };
      delete page.values[props.id];
      next[this.pageId] = page;
      return next
    })
  }

  scrollTo() {
    const refs: RegisteredRefs = this.store.state[this.pageId];
    
    if (refs.consumed || !refs.firstItemId) {
      return;
    }
    const target = refs.values[refs.firstItemId];
    const rect = target.current.getBoundingClientRect();
    const absoluteY = window.scrollY + rect.top - 50;

    window.scrollTo({
      top: absoluteY,
      behavior: "smooth",
    });
    this.consumed({ consumed: true })
  }
}

export function useScrollTo(props: { executionId: string, pageId: string }) {
  const { executionId, pageId } = props;
  const refStore = React.useMemo(() => new RefState(pageId), [executionId, pageId]);

  return ({ refStore })
}
