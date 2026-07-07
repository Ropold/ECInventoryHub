
export type Department = "MARKETING" | "ACCOUNTING" | "HR" | "DEVELOPMENT" | "MANAGEMENT";

export type EmployeeModel = {
    id: string;
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

export const DefaultEmployee: EmployeeModel = {
    id: "",
    personnelNumber: null,
    name: "",
    email: null,
    phone: null,
    address: null,
    department: "MARKETING",
    active: true,
    notes: null,
    imageUrl: null,
};