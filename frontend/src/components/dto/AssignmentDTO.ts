import { type DeviceDTO } from "./DeviceDTO.ts";
import { type EmployeeDTO } from "./EmployeeDTO.ts";
import { type AssignmentFileDTO } from "./AssignmentFileDTO.ts";

export type AssignmentDTO = {
    id: string | null;
    device: DeviceDTO;
    employee: EmployeeDTO;
    handedOutBy: EmployeeDTO | null;
    assignedDate: string;
    returnedDate: string | null;
    conditionOut: string | null;
    conditionIn: string | null;
    notes: string | null;
    copyHandedToEmployee: boolean;
    copyFiledInPersonnelFile: boolean;
    files: AssignmentFileDTO[] | null;
};