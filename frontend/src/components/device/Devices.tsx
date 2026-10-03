import type {DeviceModel} from "../models/DeviceModel.ts";
import {useState} from "react";
import {compareLocationNames, useAutoScrollToTop, useSessionState} from "../utils/ComponentsFunctions.tsx";
import SearchBar from "../SearchBar.tsx";
import DeviceCard from "./DeviceCard.tsx";
import {useNavigate} from "react-router-dom";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import NoPermissionPopup from "../NoPermissionPopup.tsx";
import {
    type DeviceStatusFilter,
    type DeviceTypeFilter,
    deviceStatusFilters,
    deviceTypeFilters
} from "../utils/DeviceFilters.ts";

type DeviceProps = {
    language: string;
    role: string;
    devices: DeviceModel[];
}

// "ALL", "NONE" (Geräte ohne Standort) oder die ID eines Standorts
type LocationFilter = string;

// Alle Standorte, an denen mindestens ein Gerät steht, in der festen Standort-Reihenfolge
function getLocationOptions(devices: DeviceModel[]): {id: string; name: string}[] {
    const byId = new Map<string, string>();
    devices.forEach((device) => {
        if (device.location) {
            byId.set(device.location.id, device.location.name);
        }
    });
    return [...byId.entries()]
        .map(([id, name]) => ({id, name}))
        .sort((a, b) => compareLocationNames(a.name, b.name));
}

function filterDevices(
    devices: DeviceModel[],
    query: string,
    statusFilter: DeviceStatusFilter,
    typeFilter: DeviceTypeFilter,
    locationFilter: LocationFilter
): DeviceModel[] {
    if (!devices) return [];

    const searchQuery = query.toLowerCase();

    return devices.filter(device => {
        if (statusFilter !== "ALL" && device.status !== statusFilter) return false;
        if (typeFilter !== "ALL" && device.type !== typeFilter) return false;
        if (locationFilter === "NONE" && device.location) return false;
        if (locationFilter !== "ALL" && locationFilter !== "NONE" && device.location?.id !== locationFilter) return false;
        return (
            device.manufacturer?.toLowerCase().includes(searchQuery) ||
            device.modelName?.toLowerCase().includes(searchQuery) ||
            device.serialNumber?.toLowerCase().includes(searchQuery) ||
            device.inventoryNumber?.toLowerCase().includes(searchQuery) ||
            device.hostname?.toLowerCase().includes(searchQuery) ||
            device.type.toLowerCase().includes(searchQuery) ||
            device.status.toLowerCase().includes(searchQuery) ||
            device.location?.name.toLowerCase().includes(searchQuery) ||
            device.notes?.toLowerCase().includes(searchQuery) ||
            device.id.toLowerCase().includes(searchQuery)
        );
    });
}

export default function Devices(props: Readonly<DeviceProps>){
    useAutoScrollToTop();
    const navigate = useNavigate();

    const [searchQuery, setSearchQuery] = useSessionState<string>("devices.searchQuery", "");
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);
    const [statusFilter, setStatusFilter] = useSessionState<DeviceStatusFilter>("devices.statusFilter", "AVAILABLE");
    const [typeFilter, setTypeFilter] = useSessionState<DeviceTypeFilter>("devices.typeFilter", "ALL");
    const [locationFilter, setLocationFilter] = useSessionState<LocationFilter>("devices.locationFilter", "ALL");

    function handleAddNewClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        navigate(`/devices/add-new-device`);
    }


    const locationOptions = getLocationOptions(props.devices);
    const locationLabel = translatedInfo["Location"][props.language];
    const filteredDevices = filterDevices(props.devices, searchQuery, statusFilter, typeFilter, locationFilter);

    return(
        <>
            <h2>Devices</h2>

            <div className={"search-add-new-button"}>
                <SearchBar
                    searchQuery={searchQuery}
                    setSearchQuery={setSearchQuery}
                    language={props.language}
                    hasActiveFilters={statusFilter !== "AVAILABLE" || typeFilter !== "ALL" || locationFilter !== "ALL"}
                    onReset={() => {
                        setStatusFilter("AVAILABLE");
                        setTypeFilter("ALL");
                        setLocationFilter("ALL");
                    }}
                />
                <select
                    className="filter-select"
                    value={typeFilter}
                    onChange={(e) => setTypeFilter(e.target.value as DeviceTypeFilter)}
                >
                    {deviceTypeFilters.map((filter) => (
                        <option key={filter.value} value={filter.value}>
                            {translatedInfo[filter.labelKey][props.language]}
                        </option>
                    ))}
                </select>
                <select
                    className="filter-select"
                    value={locationFilter}
                    onChange={(e) => setLocationFilter(e.target.value)}
                    aria-label={locationLabel}
                >
                    <option value="ALL">{locationLabel}: {translatedInfo["All"][props.language]}</option>
                    {locationOptions.map((option) => (
                        <option key={option.id} value={option.id}>
                            {locationLabel}: {option.name}
                        </option>
                    ))}
                    <option value="NONE">{locationLabel}: {translatedInfo["None"][props.language]}</option>
                </select>
                <button className="button-blue" onClick={handleAddNewClick}>{translatedInfo["New Device"][props.language]}</button>
            </div>

            <div className="filter-row">
                <div className="filter-toggle" role="group">
                    {deviceStatusFilters.map((filter) => (
                        <button
                            key={filter.value}
                            type="button"
                            className={statusFilter === filter.value ? "button-blue" : "button-grey"}
                            aria-pressed={statusFilter === filter.value}
                            onClick={() => setStatusFilter(filter.value)}
                        >
                            {translatedInfo[filter.labelKey][props.language]}
                        </button>
                    ))}
                </div>
            </div>

            {showNoPermission && (
                <NoPermissionPopup
                    language={props.language}
                    onClose={() => setShowNoPermission(false)}
                />
            )}


            <div className="device-card-container">
                {filteredDevices.map((device) => (
                    <DeviceCard key={device.id} device={device} language={props.language} />
                ))}
            </div>
        </>
    )
}
