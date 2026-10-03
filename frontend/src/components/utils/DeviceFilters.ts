import type {DeviceStatus, DeviceType} from "../models/DeviceModel.ts";

// Gemeinsame Filter-Optionen für die Listen von Geräten und Zuweisungen.
// labelKey verweist auf einen Eintrag in translatedInfo.

export type DeviceTypeFilter = DeviceType | "ALL";
export type DeviceStatusFilter = DeviceStatus | "ALL";

export const deviceTypeLabelKeys: Record<DeviceType, string> = {
    LAPTOP: "Laptop",
    PHONE: "Mobile phone",
    TABLET: "Tablet",
    MONITOR: "Monitor",
    ACCESSORY: "Accessory",
    OTHER: "Other device",
};

export const deviceTypeFilters: {value: DeviceTypeFilter; labelKey: string}[] = [
    {value: "ALL", labelKey: "All types"},
    ...(Object.keys(deviceTypeLabelKeys) as DeviceType[]).map((type) => ({value: type, labelKey: deviceTypeLabelKeys[type]})),
];

export const deviceStatusLabelKeys: Record<DeviceStatus, string> = {
    AVAILABLE: "Available",
    ASSIGNED: "Assigned",
    IN_REPAIR: "In repair",
    RETIRED: "Retired",
};

// Farbe des Status-Badges auf der Gerätekarte (Klassen aus CardBadge.css)
export const deviceStatusBadgeClasses: Record<DeviceStatus, string> = {
    AVAILABLE: "card-badge-green",
    ASSIGNED: "card-badge-blue",
    IN_REPAIR: "card-badge-yellow",
    RETIRED: "card-badge-red",
};

export const deviceTypeIcons: Record<DeviceType, string> = {
    LAPTOP: "💻",
    PHONE: "📱",
    TABLET: "📱",
    MONITOR: "🖥️",
    ACCESSORY: "🖱️",
    OTHER: "📦",
};

export const deviceStatusFilters: {value: DeviceStatusFilter; labelKey: string}[] = [
    {value: "AVAILABLE", labelKey: deviceStatusLabelKeys.AVAILABLE},
    {value: "ASSIGNED", labelKey: deviceStatusLabelKeys.ASSIGNED},
    {value: "IN_REPAIR", labelKey: deviceStatusLabelKeys.IN_REPAIR},
    {value: "RETIRED", labelKey: deviceStatusLabelKeys.RETIRED},
    {value: "ALL", labelKey: "All"},
];
