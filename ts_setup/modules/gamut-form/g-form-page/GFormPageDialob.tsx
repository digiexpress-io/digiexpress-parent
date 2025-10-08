import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GFormPage } from './GFormPage';
import { useIntl } from 'react-intl';
import { useForm } from '@dxs-ts/gamut-api';
import { GFormErrorVisibilityProvider, useGFormErrorVisibility } from '../g-form-error-visibility';



export const GFormPageDialob: React.FC<GFormBaseElementProps> = (props) => {
  return (
    <GFormErrorVisibilityProvider pageId={props.actionItem.id}>
      <Internal {...props} />
    </GFormErrorVisibilityProvider>);
}

const Internal: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store, children, disabled }) => {
  const intl = useIntl();
  const { onCancel } = useForm();
  const meta = store.form.toPage(element.id);
  const description = store.form.toDescription(element.id);
  const nextPage = meta.nextPageId ? store.form.getItem(meta.nextPageId) : undefined;
  const { setErrorsVisible } = useGFormErrorVisibility();

  // there are more page, but the backend is providing one page at a time
  let subTitle: string | undefined;
  if (meta.nextPageId && nextPage) {
    subTitle = intl.formatMessage({ id: 'gamut.forms.page.subtitle' }, { nextPageTitle: nextPage.label });

    // no more more pages
  } else if (!meta.next) {
    subTitle = intl.formatMessage({ id: 'gamut.forms.page.subtitle.complete' });
  }

  const pages: { id: string; title: string | undefined, pageNumber: number }[] = store.form.pages.map(page => ({
    id: page.id,
    title: store.form.getItem(page.id)?.label,
    pageNumber: page.order
  }));

  function onChangePage(pageId: string) {
    store.goToPage(pageId)
  }

  function onNextPage() {
    if (store.form.proceedAllowed) {
      store.next(setErrorsVisible);
    } else {
      setErrorsVisible();
    }
  }

  function onComplete() {
    if (store.form.completeAllowed) {
      store.complete(setErrorsVisible);      
    } else {
      setErrorsVisible();
    }
  }

  return (
    <GFormPage
      id={element.id}
      title={store.form.toLabel(element.id)}
      children={children}
      active={meta.active}
      pageNumber={meta.order}
      disabled={disabled}
      proceedAllowed={store.form.proceedAllowed}
      completeAllowed={store.form.completeAllowed}

      subTitle={subTitle}
      description={description}
      pages={pages}
      onChangePage={onChangePage}
      onNextPage={onNextPage}
      onComplete={onComplete}
      onCancel={onCancel}
    />);
}
