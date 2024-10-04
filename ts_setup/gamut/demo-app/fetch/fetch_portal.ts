import { CommsApi, ContractApi } from "@dxs-ts/gamut";


export function createSubjectFetch(url: (string | undefined) = 'http://localhost:8080/portal/secured/actions') {
  const fetchGet: CommsApi.GetSubjectsFetchGET = async () => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(url, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }
  return { fetchGet };
}


export function createContractFetch(url: (string | undefined) = 'http://localhost:8080/portal/secured/actions') {
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
      url + "/attachments?actionId=" + contractId,
      { method: "POST", body: JSON.stringify(body) })
      .then(resp => resp.json());

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
  return { fetchGet, appendContractAttachment };
}