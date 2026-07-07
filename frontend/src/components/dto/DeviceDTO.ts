import { type DeviceType, type DeviceStatus } from "../models/DeviceModel.ts";
import { type LocationDTO } from "./LocationDTO.ts";
import { type DeviceFileDTO } from "./DeviceFileDTO.ts";

export type DeviceDTO = {
    id: string | null;
    type: DeviceType;
    manufacturer: string | null;
    modelName: string | null;
    serialNumber: string | null;
    inventoryNumber: string | null;
    purchaseDate: string | null;
    status: DeviceStatus;
    defective: boolean;
    location: LocationDTO | null;
    notes: string | null;
    files: DeviceFileDTO[] | null;
};