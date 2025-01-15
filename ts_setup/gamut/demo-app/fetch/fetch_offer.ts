import { OfferApi } from "@dxs-ts/gamut";

export function createPublicOfferFetch(url: (string | undefined) = '/portal/feedback') {
  const fetchPost: OfferApi.CreateOfferFetchPOST = async (request: OfferApi.OfferRequest) => {

    const superTestForm = undefined//'5032fb6bbd7d5eed6daf53d0f87603d3';

    const id = superTestForm ?? request.productId;
    const locale = request.locale;
    const inputContextId = request.pageId;
    const inputParentContextId = request.parentPageId ?? "";

    const query = `${url}?actionId=${id}&actionLocale=${locale}&inputContextId=${inputContextId}&inputParentContextId=${inputParentContextId}`;

    const response = await window.fetch(query, {
      method: 'POST',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchAllGet: OfferApi.GetOffersFetchGET = async () => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(url, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }


  const fetchOneGet: OfferApi.GetOfferFetchGET = async (id) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/${id}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchDelete: OfferApi.CancelOfferFetchDELETE = async (offer) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/${offer.id}`, {
      method: 'DELETE',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }
  return { fetchAllGet, fetchOneGet, fetchPost, fetchDelete };
}


export function createOfferFetch(url: (string | undefined) = '/portal/secured/actions') {
  const fetchPost: OfferApi.CreateOfferFetchPOST = async (request: OfferApi.OfferRequest) => {

    const superTestForm = undefined//'5032fb6bbd7d5eed6daf53d0f87603d3';

    const id = superTestForm ?? request.productId;
    const locale = request.locale;
    const inputContextId = request.pageId;
    const inputParentContextId = request.parentPageId ?? "";

    const query = `${url}?id=${id}&locale=${locale}&inputContextId=${inputContextId}&inputParentContextId=${inputParentContextId}`;

    const response = await window.fetch(query, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchAllGet: OfferApi.GetOffersFetchGET = async () => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(url, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }


  const fetchOneGet: OfferApi.GetOfferFetchGET = async (id) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/${id}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchDelete: OfferApi.CancelOfferFetchDELETE = async (offer) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/${offer.id}`, {
      method: 'DELETE',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  return { fetchAllGet, fetchOneGet, fetchPost, fetchDelete };
}
