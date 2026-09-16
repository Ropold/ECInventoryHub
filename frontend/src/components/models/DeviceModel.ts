import { type LocationModel, DefaultLocation } from "./LocationModel.ts";
import { type DeviceFileModel } from "./DeviceFileModel.ts";

export type DeviceType = "LAPTOP" | "PHONE" | "TABLET" | "MONITOR" | "ACCESSORY" | "OTHER";
export type DeviceStatus = "AVAILABLE" | "ASSIGNED" | "IN_REPAIR" | "RETIRED";

export type DeviceModel = {
    id: string;
    type: DeviceType;
    manufacturer: string | null;
    modelName: string | null;
    serialNumber: string | null;
    inventoryNumber: string | null;
    purchaseDate: string | null;
    status: DeviceStatus;
    defective: boolean;
    location: LocationModel | null;
    notes: string | null;
    files: DeviceFileModel[];
};

export const DefaultDevice: DeviceModel = {
    id: "",
    type: "OTHER",
    manufacturer: null,
    modelName: null,
    serialNumber: null,
    inventoryNumber: null,
    purchaseDate: null,
    status: "AVAILABLE",
    defective: false,
    location: DefaultLocation,
    notes: null,
    files: [],
};