import { DialobApi } from './dialob-types';

export function parsePage(currentPageId: string, session: DialobApi.Form): DialobApi.ControlPage {
  const questionnaire = session.getItem('questionnaire');
  const allPageIds = questionnaire?.items ?? [];
  const activePageId = questionnaire?.activeItem ?? '';
  const isPageActive = activePageId === currentPageId;

  const lastPageId: string = getLastPageId(session, allPageIds) ?? currentPageId;
  const completeEnabled: boolean = !!questionnaire?.allowedActions?.includes("COMPLETE");
  const isLastPage = currentPageId === lastPageId;
  // is the page 'todo' or it has already been filled 'completed'?
  const currentlyFilling = allPageIds.indexOf(activePageId);
  const whereIsCurrentItem = allPageIds.indexOf(currentPageId);



  const pages: DialobApi.ActionItem[] = allPageIds
    .map(delegateId => session.getItem(delegateId))
    .filter(junk_data => junk_data ? true : false)
    .map(p => p as DialobApi.ActionItem)


  const singular: boolean = pages.length === 1
  let status: 'completed' | 'filling' | 'todo' | 'submit';
  if (singular) {
    status = 'filling';
  } else if (completeEnabled && isLastPage) {
    status = 'submit';
  } else if (isPageActive) {
    status = 'filling';
  } else {
    status = 'todo';
  }

  if (!isPageActive) {
    status = currentlyFilling > whereIsCurrentItem ? 'completed' : 'todo';
  }



  const currentPage: DialobApi.ControlPage = {
    id: currentPageId,
    summary: (status === 'completed' || status === 'submit'),
    active: isPageActive,
    next: !!questionnaire?.allowedActions?.includes("NEXT"),
    status,
    submitted: status === 'completed',
    singular,
    order: getPageNumber(allPageIds, currentPageId, session),
    nextPageId: isLastPage ? undefined : allPageIds[allPageIds.indexOf(currentPageId) + 1],
  };

  return currentPage;
}

function getLastPageId(session: DialobApi.Form, availableItems: string[]): string | undefined {
  let lastPageId: string | undefined = undefined;
  for (const pageId of availableItems) {
    const page = session.getItem(pageId);
    if (page) {
      lastPageId = page.id;
    }
  }

  return lastPageId;
}

function getPageNumber(allPageIds: string[], pageId: string, session: DialobApi.Form): number {
  const index = allPageIds
    .map(delegateId => session.getItem(delegateId))
    .filter(junk_data => junk_data)
    .map(item => item?.id)
    .indexOf(pageId);

  return index + 1;
}