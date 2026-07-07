import { type DeviceModel, DefaultDevice } from "./DeviceModel.ts";
import { type EmployeeModel, DefaultEmployee } from "./EmployeeModel.ts";
import { type AssignmentFileModel } from "./AssignmentFileModel.ts";

export type AssignmentModel = {
    id: string;
    device: DeviceModel;
    employee: EmployeeModel;
    handedOutBy: EmployeeModel | null;
    assignedDate: string;
    returnedDate: string | null;
    conditionOut: string | null;
    conditionIn: string | null;
    notes: string | null;
    copyHandedToEmployee: boolean;
    copyFiledInPersonnelFile: boolean;
    files: AssignmentFileModel[];
};

export const DefaultAssignment: AssignmentModel = {
    id: "",
    device: DefaultDevice,
    employee: DefaultEmployee,
    handedOutBy: null,
    assignedDate: "",
    returnedDate: null,
    conditionOut: null,
    conditionIn: null,
    notes: null,
    copyHandedToEmployee: false,
    copyFiledInPersonnelFile: false,
    files: [],
};