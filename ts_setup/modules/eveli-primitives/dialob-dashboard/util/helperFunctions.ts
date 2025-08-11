import FileSaver from 'file-saver';

export const extractDate = (dateString: string | Date) => {
	if (!dateString) return null;
	const date = new Date(dateString);
	date.setHours(0, 0, 0, 0);
	return date;
}

export const downloadAsJSON = (data: any) => {
	let fileName = undefined;
	if (Array.isArray(data)) {
		fileName = 'dialobForms.json'
	} else {
		fileName = data?.metadata?.label ? `${data.metadata.label}.json` : 'data.json';
	}
	const blob = new Blob([JSON.stringify(data)], { type: 'json' });
	FileSaver.saveAs(blob, fileName);
}
