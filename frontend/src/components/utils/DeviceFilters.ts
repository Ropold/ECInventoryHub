import type {DeviceStatus, DeviceType} from "../models/DeviceModel.ts";

// Gemeinsame Filter-Optionen für die Listen von Geräten und Zuweisungen.
// labelKey verweist auf einen Eintrag in translatedInfo.

export type DeviceTypeFilter = DeviceType | "ALL";
export type DeviceStatusFilter = DeviceStatus | "ALL";

export const deviceTypeFilters: {value: DeviceTypeFilter; labelKey: string}[] = [
    {value: "ALL", labelKey: "All types"},
    {value: "LAPTOP", labelKey: "Laptop"},
    {value: "PHONE", labelKey: "Mobile phone"},
    {value: "TABLET", labelKey: "Tablet"},
    {value: "MONITOR", labelKey: "Monitor"},
    {value: "ACCESSORY", labelKey: "Accessory"},
    {value: "OTHER", labelKey: "Other device"},
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
