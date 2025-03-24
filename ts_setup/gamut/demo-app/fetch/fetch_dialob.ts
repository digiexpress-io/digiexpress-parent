import { DialobApi } from "@dxs-ts/gamut";


export function createPublicDialobFetch(url: (string | undefined) = '/portal/feedback') {
  const fetchActionPost: DialobApi.FetchActionPOST = async (sessionId: string, actions: DialobApi.Action[], rev: number) => {
    const response = await window.fetch(`${url}/fill/${sessionId}`, {
      method: 'POST',
      body: JSON.stringify({ rev, actions }),
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchActionGet: DialobApi.FetchActionGET = async (sessionId: string) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/fill/${sessionId}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchReviewGet: DialobApi.FetchReviewGET = async (sessionId: string) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/review/${sessionId}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }
  return { fetchActionGet, fetchActionPost, fetchReviewGet };
}


export function createDialobFetch(url: (string | undefined) = '/portal/secured/actions') {
  const fetchActionPost: DialobApi.FetchActionPOST = async (sessionId: string, actions: DialobApi.Action[], rev: number) => {
    const response = await window.fetch(`${url}/fill/${sessionId}`, {
      method: 'POST',
      body: JSON.stringify({ rev, actions }),
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchActionGet: DialobApi.FetchActionGET = async (sessionId: string) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/fill/${sessionId}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchReviewGet: DialobApi.FetchReviewGET = async (sessionId: string) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/review/${sessionId}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }



  const fetchAttachmentPost: DialobApi.FetchAttachmentPOST = async (procId, files) =>{
    const filesByName: Record<string, File> = {};
    const body: { name: string, fileType: string }[] = [];
    for (const file of Array.from(files)) {
      body.push({
        name: file.name,
        fileType: file.type || 'application/octet-stream',
      });
      filesByName[file.name] = file;
    }
    const uploadUrls = await window.fetch(
      `${url}/${procId}/attachments`,
      { method: "POST", 
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body), 
       })
      .then((resp) => resp.json())
      .then((data: { upload: string, name: string }[]) => data);

    for (const url of uploadUrls) {
      const file = filesByName[url.name];
      const uploadedFile = await fetch(url.upload, { method: 'PUT', body: file, headers: { 'Content-Type': file.type || 'application/octet-stream' } });
      console.log('uploaded', url.name, uploadedFile);
    }
  }

  return { fetchActionGet, fetchActionPost, fetchReviewGet, fetchAttachmentPost };
}

