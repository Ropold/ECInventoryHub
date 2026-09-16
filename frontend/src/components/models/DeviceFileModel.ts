
export type DeviceFileModel = {
    id: string;
    fileUrl: string;
    fileType: string | null;
    uploadedAt: string;
};

export const DefaultDeviceFile: DeviceFileModel = {
    id: "",
    fileUrl: "",
    fileType: null,
    uploadedAt: "",
};