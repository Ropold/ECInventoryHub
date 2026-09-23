import type {DeviceModel} from "../models/DeviceModel.ts";

export type LocationDeviceStats = {
    total: number;
    inRepair: number;
    defective: number;
};

// Zählt die Geräte pro Standort (Key = location.id). Geräte ohne Standort werden ignoriert.
export function getDeviceStatsByLocation(devices: DeviceModel[]): Record<string, LocationDeviceStats> {
    const stats: Record<string, LocationDeviceStats> = {};

    devices.forEach((device) => {
        const locationId = device.location?.id;
        if (!locationId) return;

        const entry = stats[locationId] ?? {total: 0, inRepair: 0, defective: 0};
        entry.total++;
        if (device.status === "IN_REPAIR") entry.inRepair++;
        if (device.defective) entry.defective++;
        stats[locationId] = entry;
    });

    return stats;
}
