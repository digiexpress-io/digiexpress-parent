import { CommsApi, ContractApi, SiteApi } from "@dxs-ts/gamut";



export function createAuthFeedbackFetch(url: (string | undefined) = '/portal/secured/actions') {
  const fetchFeedbackRatingPut: SiteApi.FetchFeedbackRatingPUT = async (body) => {
    const response = await window.fetch(`${url}/feedback`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: undefined,
      body: JSON.stringify(body),
    });
    return response;
  }
  const fetchFeedbackGet: SiteApi.FetchSiteGET = async (locale: string) => {
    const response = await window.fetch(`${url}/feedback?locale=${locale}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  return {
    fetchFeedbackRatingPut, fetchFeedbackGet
  };
}
export function createSubjectFetch(url: (string | undefined) = '/portal/secured/actions') {
  const fetchGet: CommsApi.GetSubjectsFetchGET = async () => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(url, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchPost: CommsApi.ReplyToFetchPOST = async (replyTo) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(url + '/' + replyTo.subjectId + '/messages', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: undefined,
      body: JSON.stringify(replyTo)
    });
    return response;
  }


  const fetchPut: CommsApi.ViewSubjectFetchPUT = async (subjectId) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(url + '/' + subjectId + '/views', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: undefined
    });
    return response;
  }
  return { fetchGet, fetchPost, fetchPut };
}


export function createContractFetch(url: (string | undefined) = '/portal/secured/actions') {
  const appendContractAttachment: ContractApi.AppendContractAttachmentFetchPOST = async (contractId: ContractApi.ContractId, files: FileList) => {
    const filesByName: Record<string, File> = {};
    const body: { fileName: string, fileType: string }[] = [];
    for (const file of Array.from(files)) {
      body.push({
        fileName: file.name,
        fileType: file.type || 'application/octet-stream',
      });
      filesByName[file.name] = file;
    }

    const uploadUrls: { upload: string, name: string }[] = await window.fetch(
      `${url}/${contractId}/attachments`,
      { method: "POST", body: JSON.stringify(body) })
      .then(resp => resp.json()
      );

    for (const url of uploadUrls) {
      const file = filesByName[url.name];
      const uploadedFile = await fetch(url.upload, { method: 'PUT', body: file, headers: { 'Content-Type': file.type || 'application/octet-stream' } });
      console.log('uploaded', url.name, uploadedFile);
    }
  }

  const fetchGet: ContractApi.GetContractFetchGET = async () => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(url, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchContractAttachment: ContractApi.ContractAttachmentFetchGET = async (id, filename) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/${id}/attachments/${filename}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }
  return { fetchGet, appendContractAttachment, fetchContractAttachment};
}