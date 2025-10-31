import React from "react";

export interface DebugPdfViewerProps {
  base64: string;
  fileName?: string;
}

export const DebugPdfViewer: React.FC<DebugPdfViewerProps> = ({ base64, fileName = 'document.pdf' }) => {
  const [pdfUrl, setPdfUrl] = React.useState<string>('');

  React.useEffect(() => {
    const binaryString = atob(base64);
    const bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i);
    }
    const blob = new Blob([bytes], { type: 'application/pdf' });
    const url = URL.createObjectURL(blob);
    setPdfUrl(url);

    return () => URL.revokeObjectURL(url);
  }, [base64]);

  if(!base64) {
    return <>NO PDF</>
  }

  return (
    <iframe
      src={pdfUrl}
      width="100%"
      height="100%"
      title={fileName}
    />
  );
}