
export type AssignmentFileModel = {
    id: string;
    fileUrl: string;
    fileType: string | null;
    uploadedAt: string;
};

export const DefaultAssignmentFile: AssignmentFileModel = {
    id: "",
    fileUrl: "",
    fileType: null,
    uploadedAt: "",
};