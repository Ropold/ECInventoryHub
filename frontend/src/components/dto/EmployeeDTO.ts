import { type Department } from "../models/EmployeeModel.ts";

export type EmployeeDTO = {
    id: string | null;
    personnelNumber: string | null;
    name: string;
    email: string | null;
    phone: string | null;
    address: string | null;
    department: Department;
    active: boolean;
    notes: string | null;
    imageUrl: string | null;
};